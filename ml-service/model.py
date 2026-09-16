import pandas as pd
import joblib

from sklearn.ensemble import RandomForestClassifier


# Training data
data = {
    "amount": [
        500,
        1000,
        2500,
        5000,
        10000,
        20000,
        50000,
        75000,
        100000,
        150000,
        200000,
        300000,
    ],
    "unknown_device": [
        0, 0, 0, 0,
        0, 0, 1, 1,
        1, 1, 1, 1,
    ],
    "unknown_location": [
        0, 0, 0, 0,
        0, 0, 0, 1,
        1, 1, 1, 1,
    ],
    "fraud": [
        0, 0, 0, 0,
        0, 0, 0, 0,
        1, 1, 1, 1,
    ],
}


df = pd.DataFrame(data)

X = df[
    [
        "amount",
        "unknown_device",
        "unknown_location",
    ]
]

y = df["fraud"]


# Train Random Forest model
model = RandomForestClassifier(
    n_estimators=100,
    random_state=42,
)

model.fit(X, y)


# Save trained model
joblib.dump(model, "fraud_model.joblib")

print("Fraud detection ML model trained successfully.")
print("Model saved as fraud_model.joblib")