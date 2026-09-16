import { useEffect, useState } from "react";
import axios from "axios";
import "./App.css";

function App() {
  const [transactions, setTransactions] = useState([]);
  const [riskResults, setRiskResults] = useState({});
  const [formData, setFormData] = useState({
    customerId: "",
    amount: "",
    currency: "INR",
    merchant: "",
    location: "",
    transactionType: "ONLINE",
    deviceId: "",
  });

  const [message, setMessage] = useState("");

  const highRiskCount = Object.values(riskResults).filter(
    (result) => result.riskLevel === "HIGH",
  ).length;

  const mediumRiskCount = Object.values(riskResults).filter(
    (result) => result.riskLevel === "MEDIUM",
  ).length;

  const lowRiskCount = Object.values(riskResults).filter(
    (result) => result.riskLevel === "LOW",
  ).length;

  // Analyze one transaction
  const analyzeTransaction = (transaction) => {
    return axios
      .post("http://localhost:8080/api/fraud/analyze", {
        id: transaction.id,
        customerId: transaction.customerId,
        amount: transaction.amount,
        currency: transaction.currency,
        merchant: transaction.merchant,
        location: transaction.location,
        transactionType: transaction.transactionType,
        deviceId: transaction.deviceId,
        timestamp: transaction.timestamp,

        // Dashboard analysis must NOT send a Kafka alert
        sendAlert: false,
      })
      .then((response) => {
        return {
          id: transaction.id,
          result: response.data,
        };
      })
      .catch((error) => {
        console.error("Error analyzing transaction:", transaction.id, error);

        return null;
      });
  };

  // Fetch transactions
  const fetchTransactions = () => {
    return axios
      .get("http://localhost:8080/api/transactions")
      .then((response) => {
        setTransactions(response.data);
        return response.data;
      })
      .catch((error) => {
        console.error("Error fetching transactions:", error);
        return [];
      });
  };

  // Analyze all existing transactions once
  const analyzeExistingTransactions = (transactionList) => {
    if (!transactionList || transactionList.length === 0) {
      return;
    }

    const analysisRequests = transactionList.map((transaction) =>
      analyzeTransaction(transaction),
    );

    Promise.all(analysisRequests).then((results) => {
      const newRiskResults = {};

      results.forEach((item) => {
        if (item) {
          newRiskResults[item.id] = item.result;
        }
      });

      setRiskResults(newRiskResults);
    });
  };

  useEffect(() => {
    // First dashboard load
    fetchTransactions().then((transactionList) => {
      // Analyze existing transactions only once
      analyzeExistingTransactions(transactionList);
    });

    // Refresh transaction list every 5 seconds.
    // IMPORTANT:
    // We do NOT analyze transactions again here.
    const interval = setInterval(() => {
      fetchTransactions();
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleChange = (event) => {
    setFormData({
      ...formData,
      [event.target.name]: event.target.value,
    });
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    setMessage("Creating transaction...");

    axios
      .post("http://localhost:8080/api/transactions", {
        customerId: formData.customerId,
        amount: Number(formData.amount),
        currency: formData.currency,
        merchant: formData.merchant,
        location: formData.location,
        transactionType: formData.transactionType,
        deviceId: formData.deviceId,
      })
      .then((response) => {
        const newTransaction = response.data;

        setMessage("Transaction created successfully!");

        setFormData({
          customerId: "",
          amount: "",
          currency: "INR",
          merchant: "",
          location: "",
          transactionType: "ONLINE",
          deviceId: "",
        });

        // Add new transaction immediately
        setTransactions((previousTransactions) => [
          ...previousTransactions,
          newTransaction,
        ]);

        // Analyze new transaction exactly once.
        // The backend Kafka flow already handles the real alert.
        analyzeTransaction(newTransaction).then((item) => {
          if (item) {
            setRiskResults((previousResults) => ({
              ...previousResults,
              [item.id]: item.result,
            }));
          }
        });
      })
      .catch((error) => {
        console.error("Error creating transaction:", error);
        setMessage("Failed to create transaction.");
      });
  };

  return (
    <div className="dashboard">
      <header className="header">
        <div>
          <h1>Fraud Detection System</h1>
          <p>Real-Time Transaction Monitoring</p>
        </div>

        <div className="status">
          <span className="status-dot"></span>
          System Online
        </div>
      </header>

      <section className="cards">
        <div className="card">
          <h3>Total Transactions</h3>
          <p>{transactions.length}</p>
        </div>

        <div className="card">
          <h3>High Risk</h3>
          <p>{highRiskCount}</p>
        </div>

        <div className="card">
          <h3>Medium Risk</h3>
          <p>{mediumRiskCount}</p>
        </div>

        <div className="card">
          <h3>Low Risk</h3>
          <p>{lowRiskCount}</p>
        </div>
      </section>

      <section className="transactions">
        <h2>Create New Transaction</h2>

        <form className="transaction-form" onSubmit={handleSubmit}>
          <input
            type="text"
            name="customerId"
            placeholder="Customer ID"
            value={formData.customerId}
            onChange={handleChange}
            required
          />

          <input
            type="number"
            name="amount"
            placeholder="Amount"
            value={formData.amount}
            onChange={handleChange}
            required
          />

          <input
            type="text"
            name="currency"
            placeholder="Currency"
            value={formData.currency}
            onChange={handleChange}
            required
          />

          <input
            type="text"
            name="merchant"
            placeholder="Merchant"
            value={formData.merchant}
            onChange={handleChange}
            required
          />

          <input
            type="text"
            name="location"
            placeholder="Location"
            value={formData.location}
            onChange={handleChange}
            required
          />

          <select
            name="transactionType"
            value={formData.transactionType}
            onChange={handleChange}
          >
            <option value="ONLINE">ONLINE</option>
            <option value="POS">POS</option>
            <option value="ATM">ATM</option>
          </select>

          <input
            type="text"
            name="deviceId"
            placeholder="Device ID"
            value={formData.deviceId}
            onChange={handleChange}
            required
          />

          <button type="submit">Create Transaction</button>
        </form>

        {message && <p className="form-message">{message}</p>}
      </section>

      <section className="transactions">
        <h2>Recent Transactions</h2>

        {transactions.length === 0 ? (
          <div className="empty">
            <p>No transactions available</p>
            <span>Transactions will appear here in real time.</span>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Customer ID</th>
                  <th>Amount</th>
                  <th>Merchant</th>
                  <th>Location</th>
                  <th>Type</th>
                  <th>Device</th>
                  <th>Timestamp</th>
                  <th>Risk Score</th>
                  <th>Risk Level</th>
                  <th>Fraud Reasons</th>
                </tr>
              </thead>

              <tbody>
                {transactions.map((transaction) => {
                  const result = riskResults[transaction.id];

                  return (
                    <tr key={transaction.id}>
                      <td>{transaction.customerId}</td>

                      <td>₹{transaction.amount}</td>

                      <td>{transaction.merchant}</td>

                      <td>{transaction.location}</td>

                      <td>{transaction.transactionType}</td>

                      <td>{transaction.deviceId}</td>

                      <td>{transaction.timestamp}</td>

                      <td>{result?.riskScore ?? "Analyzing..."}</td>

                      <td>
                        <span
                          className={`risk-badge ${
                            result?.riskLevel?.toLowerCase() || "analyzing"
                          }`}
                        >
                          {result?.riskLevel || "Analyzing..."}
                        </span>
                      </td>

                      <td>
                        {result?.reasons?.length > 0 ? (
                          <ul className="fraud-reasons">
                            {result.reasons.map((reason, reasonIndex) => (
                              <li key={reasonIndex}>{reason}</li>
                            ))}
                          </ul>
                        ) : result ? (
                          <span className="no-risk-reason">
                            No fraud indicators
                          </span>
                        ) : (
                          "Analyzing..."
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default App;
