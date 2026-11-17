#!/usr/bin/env python3
"""Entraîne un modèle binaire didactique avec heart.csv et l'exporte vers Android.

Exemples :
    python ml/train_export.py --download
    python ml/train_export.py --csv ml/heart.csv

Le score exporté n'est PAS un diagnostic ni un risque clinique validé.
"""
import argparse
import json
import urllib.request
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

ROOT = Path(__file__).resolve().parent.parent
CSV_DEFAULT = ROOT / "ml" / "heart.csv"
MODEL_OUT = ROOT / "app" / "src" / "main" / "assets" / "heart_model.json"
SOURCE = "https://www.kaggle.com/datasets/fedesoriano/heart-failure-prediction"
MIRROR = "https://raw.githubusercontent.com/xpy-10/DataSet/main/heart.csv"
NUM = ["Age", "RestingBP", "Cholesterol", "FastingBS", "MaxHR", "Oldpeak"]
CATS = {
    "Sex": ["M", "F"],
    "ChestPainType": ["TA", "ATA", "NAP", "ASY"],
    "RestingECG": ["Normal", "ST", "LVH"],
    "ExerciseAngina": ["Y", "N"],
    "ST_Slope": ["Up", "Flat", "Down"],
}
FEATURES = NUM + [f"{col}={category}" for col, categories in CATS.items() for category in categories]
REQUIRED = NUM + list(CATS) + ["HeartDisease"]


def vectorize(df: pd.DataFrame) -> np.ndarray:
    parts = [pd.to_numeric(df[col], errors="raise").to_numpy(dtype=float) for col in NUM]
    for col, categories in CATS.items():
        unknown = set(df[col].astype(str).unique()) - set(categories)
        if unknown:
            raise ValueError(f"Valeurs inconnues pour {col}: {sorted(unknown)}")
        for category in categories:
            parts.append((df[col].astype(str) == category).to_numpy(dtype=float))
    return np.column_stack(parts)


def train(csv_path: Path, output_path: Path = MODEL_OUT) -> dict:
    if not csv_path.exists():
        raise FileNotFoundError(f"Dataset introuvable : {csv_path}. Téléchargez heart.csv depuis {SOURCE}.")
    df = pd.read_csv(csv_path)
    missing = set(REQUIRED) - set(df.columns)
    if missing:
        raise ValueError(f"Colonnes manquantes: {sorted(missing)}")
    if df[REQUIRED].isnull().any().any():
        raise ValueError("Le dataset contient des valeurs manquantes dans les colonnes nécessaires.")
    X = vectorize(df)
    y_numeric = pd.to_numeric(df["HeartDisease"], errors="raise").to_numpy(dtype=float)
    if not np.isfinite(X).all():
        raise ValueError("Des valeurs non finies ont été détectées dans les variables d'entrée.")
    if not np.isin(y_numeric, [0.0, 1.0]).all() or set(np.unique(y_numeric)) != {0.0, 1.0}:
        raise ValueError("Cible HeartDisease : valeurs exactes 0 et 1 requises.")
    y = y_numeric.astype(int)
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    scaler = StandardScaler().fit(X_train)
    model = LogisticRegression(max_iter=5000, random_state=42, class_weight="balanced")
    model.fit(scaler.transform(X_train), y_train)
    scores = model.predict_proba(scaler.transform(X_test))[:, 1]
    predicted = (scores >= 0.5).astype(int)
    artifact = {
        "format_version": 1,
        "source_dataset": SOURCE,
        "source_csv": str(csv_path.name),
        "training_rows": int(len(X_train)),
        "total_rows": int(len(df)),
        "test_rows": int(len(X_test)),
        "test_accuracy": float(accuracy_score(y_test, predicted)),
        "test_roc_auc": float(roc_auc_score(y_test, scores)),
        "features": FEATURES,
        "means": scaler.mean_.tolist(),
        "scales": scaler.scale_.tolist(),
        "coefficients": model.coef_[0].tolist(),
        "intercept": float(model.intercept_[0]),
        "label_definition": "HeartDisease: 1=presence, 0=absence in dataset",
        "warning": "Prototype pédagogique non validé cliniquement. Ne pas utiliser pour le diagnostic.",
    }
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(artifact, indent=2, ensure_ascii=False), encoding="utf-8")
    # Vérification mathématique de l'export : formule Python égale à sklearn.
    t = (X_test[0] - scaler.mean_) / scaler.scale_
    exported = 1 / (1 + np.exp(-(artifact["intercept"] + t @ model.coef_[0])))
    assert abs(float(exported) - float(scores[0])) < 1e-9
    return artifact


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--csv", type=Path, default=CSV_DEFAULT)
    p.add_argument("--download", action="store_true",
                   help="Télécharge un miroir public du dataset Kaggle (internet requis)")
    args = p.parse_args()
    if args.download:
        print(f"Téléchargement public : {MIRROR}")
        try:
            urllib.request.urlretrieve(MIRROR, args.csv)
        except Exception as exc:
            raise RuntimeError("Téléchargement impossible. Placez heart.csv dans ml/ puis relancez sans --download.") from exc
    artifact = train(args.csv)
    print(f"OK : {artifact['total_rows']} lignes; train={artifact['training_rows']}, test={artifact['test_rows']}")
    print(f"Accuracy test={artifact['test_accuracy']:.3f}, ROC-AUC test={artifact['test_roc_auc']:.3f}")
    print(f"Modèle enregistré : {MODEL_OUT}")
    print("Important : il s'agit d'une classification pédagogique non validée cliniquement.")


if __name__ == "__main__":
    main()
