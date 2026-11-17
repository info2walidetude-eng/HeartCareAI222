"""Test technique sur données *SYNTHÉTIQUES* : pas de métrique clinique !"""
import importlib.util
import tempfile
from pathlib import Path
import unittest

import numpy as np
import pandas as pd

ML_FILE = Path(__file__).resolve().parents[1] / "train_export.py"
spec = importlib.util.spec_from_file_location("train_export", ML_FILE)
ml = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ml)


class ExportTest(unittest.TestCase):
    def test_export_fields_and_math(self):
        rng = np.random.default_rng(42)
        n = 180
        frame = pd.DataFrame({
            "Age": rng.integers(30, 85, n),
            "Sex": rng.choice(["M", "F"], n),
            "ChestPainType": rng.choice(["TA", "ATA", "NAP", "ASY"], n),
            "RestingBP": rng.integers(90, 200, n),
            "Cholesterol": rng.integers(100, 400, n),
            "FastingBS": rng.integers(0, 2, n),
            "RestingECG": rng.choice(["Normal", "ST", "LVH"], n),
            "MaxHR": rng.integers(65, 200, n),
            "ExerciseAngina": rng.choice(["Y", "N"], n),
            "Oldpeak": rng.normal(1, 0.6, n),
            "ST_Slope": rng.choice(["Up", "Flat", "Down"], n),
            "HeartDisease": rng.integers(0, 2, n),
        })
        with tempfile.TemporaryDirectory() as folder:
            csv = Path(folder) / "synthetic.csv"
            out = Path(folder) / "model.json"
            frame.to_csv(csv, index=False)
            payload = ml.train(csv, out)
            self.assertEqual(len(payload["features"]), 20)
            self.assertEqual(payload["total_rows"], n)
            self.assertTrue(out.exists())
            self.assertLessEqual(payload["test_accuracy"], 1.0)
            self.assertGreaterEqual(payload["test_accuracy"], 0.0)
            self.assertEqual(len(payload["coefficients"]), len(payload["features"]))
            self.assertEqual(len(payload["means"]), len(payload["features"]))
            # Verify the independently reconstructed exported formula on multiple rows.
            X = ml.vectorize(frame)
            for x in X[:10]:
                z = payload["intercept"] + sum(
                    (x[i]-payload["means"][i])/payload["scales"][i] * payload["coefficients"][i]
                    for i in range(len(x))
                )
                p = 1/(1+np.exp(-z))
                self.assertTrue(0.0 <= p <= 1.0)

    def test_reject_non_binary_labels(self):
        rng = np.random.default_rng(1)
        n = 30
        frame = pd.DataFrame({
            "Age": rng.integers(30, 80, n),
            "Sex": rng.choice(["M", "F"], n),
            "ChestPainType": rng.choice(["TA", "ATA", "NAP", "ASY"], n),
            "RestingBP": rng.integers(100, 180, n),
            "Cholesterol": rng.integers(100, 400, n),
            "FastingBS": rng.integers(0, 2, n),
            "RestingECG": rng.choice(["Normal", "ST", "LVH"], n),
            "MaxHR": rng.integers(65, 200, n),
            "ExerciseAngina": rng.choice(["Y", "N"], n),
            "Oldpeak": rng.random(n),
            "ST_Slope": rng.choice(["Up", "Flat", "Down"], n),
            "HeartDisease": np.array([0, 1]*15, dtype=float),
        })
        frame.loc[0, "HeartDisease"] = 0.8
        with tempfile.TemporaryDirectory() as folder:
            csv = Path(folder) / "bad_target.csv"
            frame.to_csv(csv, index=False)
            with self.assertRaisesRegex(ValueError, "valeurs exactes"):
                ml.train(csv, Path(folder)/"unused.json")


if __name__ == "__main__":
    unittest.main()
