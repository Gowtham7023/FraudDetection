from fastapi import FastAPI
from pydantic import BaseModel
import joblib


app = FastAPI(title="Fraud Detection ML Service")


# Load trained model
model = joblib.load("fraud_model.joblib")


class Transaction(BaseModel):
    amount: float
    unknown_device: int
    unknown_location: int


@app.get("/")
def home():
    return {
        "service": "Fraud Detection ML Service",
        "status": "running"
    }


@app.post("/predict")
def predict(transaction: Transaction):

    features = [[
        transaction.amount,
        transaction.unknown_device,
        transaction.unknown_location
    ]]

    prediction = model.predict(features)[0]

    probability = model.predict_proba(features)[0][1]

    ml_risk_score = round(float(probability) * 100, 2)

    return {
        "fraud_prediction": int(prediction),
        "ml_risk_score": ml_risk_score
    }