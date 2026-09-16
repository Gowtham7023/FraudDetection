import { useEffect, useState } from "react";
import axios from "axios";
import "./App.css";

function App() {
  const [transactions, setTransactions] = useState([]);
  const [riskResults, setRiskResults] = useState({});
  const [alerts, setAlerts] = useState([]);
  const [previousAlertCount, setPreviousAlertCount] = useState(0);
  const [newAlert, setNewAlert] = useState(false);

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

  // =========================
  // FRAUD ANALYSIS
  // =========================

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

  // =========================
  // FETCH TRANSACTIONS
  // =========================

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

  // =========================
  // FETCH FRAUD ALERTS
  // =========================

  const fetchAlerts = () => {
    axios
      .get("http://localhost:8082/api/alerts")
      .then((response) => {
        const parsedAlerts = response.data
          .map((alert) => {
            try {
              return JSON.parse(alert);
            } catch (error) {
              console.error("Invalid alert JSON:", alert);
              return null;
            }
          })
          .filter((alert) => alert !== null)
          .reverse();

        setAlerts(parsedAlerts);

        // Detect newly received fraud alert
        if (
          previousAlertCount !== 0 &&
          parsedAlerts.length > previousAlertCount
        ) {
          setNewAlert(true);

          setTimeout(() => {
            setNewAlert(false);
          }, 4000);
        }

        setPreviousAlertCount(parsedAlerts.length);
      })
      .catch((error) => {
        console.error("Error fetching fraud alerts:", error);
      });
  };

  // =========================
  // ANALYZE EXISTING
  // =========================

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

  // =========================
  // INITIAL LOAD + REFRESH
  // =========================

  useEffect(() => {
    fetchTransactions().then((transactionList) => {
      analyzeExistingTransactions(transactionList);
    });

    fetchAlerts();

    const interval = setInterval(() => {
      fetchTransactions();
      fetchAlerts();
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  // =========================
  // FORM HANDLING
  // =========================

  const handleChange = (event) => {
    setFormData({
      ...formData,
      [event.target.name]: event.target.value,
    });
  };

  // =========================
  // CREATE TRANSACTION
  // =========================

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

        setTransactions((previousTransactions) => [
          ...previousTransactions,
          newTransaction,
        ]);

        analyzeTransaction(newTransaction).then((item) => {
          if (item) {
            setRiskResults((previousResults) => ({
              ...previousResults,
              [item.id]: item.result,
            }));
          }
        });

        // Refresh alerts after creating a transaction
        setTimeout(() => {
          fetchAlerts();
        }, 1500);
      })
      .catch((error) => {
        console.error("Error creating transaction:", error);
        setMessage("Failed to create transaction.");
      });
  };

  // =========================
  // HELPERS
  // =========================

  const getRiskClass = (riskLevel) => {
    if (!riskLevel) {
      return "analyzing";
    }

    return riskLevel.toLowerCase();
  };

  return (
    <div className="dashboard">
      {/* =========================
          HEADER
      ========================= */}

      <header className="header">
        <div className="brand-section">
          <div className="brand-icon">🛡️</div>

          <div>
            <h1>Fraud Detection System</h1>
            <p>Real-Time Transaction Monitoring</p>
          </div>
        </div>

        <div className="system-status">
          <span className="status-dot"></span>
          <span>System Online</span>
        </div>
      </header>

      {/* =========================
          LIVE DETECTION BANNER
      ========================= */}

      {newAlert && (
        <div className="live-fraud-banner">
          <span className="live-fraud-icon">🚨</span>

          <div>
            <strong>New Fraud Alert Detected</strong>
            <p>
              A high-risk transaction was detected by the real-time fraud
              detection pipeline.
            </p>
          </div>
        </div>
      )}

      {/* =========================
          STATISTICS
      ========================= */}

      <section className="stats-grid">
        <div className="stat-card total-card">
          <div className="stat-icon">📊</div>

          <div>
            <p className="stat-label">Total Transactions</p>
            <h2>{transactions.length}</h2>
          </div>
        </div>

        <div className="stat-card high-card">
          <div className="stat-icon">🚨</div>

          <div>
            <p className="stat-label">High Risk</p>
            <h2>{highRiskCount}</h2>
          </div>
        </div>

        <div className="stat-card medium-card">
          <div className="stat-icon">⚠️</div>

          <div>
            <p className="stat-label">Medium Risk</p>
            <h2>{mediumRiskCount}</h2>
          </div>
        </div>

        <div className="stat-card low-card">
          <div className="stat-icon">✓</div>

          <div>
            <p className="stat-label">Low Risk</p>
            <h2>{lowRiskCount}</h2>
          </div>
        </div>
      </section>

      {/* =========================
          FRAUD ALERTS
      ========================= */}

      <section className="panel alerts-panel">
        <div className="panel-header transaction-header">
          <div>
            <h2>🚨 Fraud Alerts</h2>

            <p>Real-time high-risk transactions detected by the system</p>
          </div>

          <div className="alert-count">
            {alerts.length} Alert{alerts.length !== 1 ? "s" : ""}
          </div>
        </div>

        {alerts.length === 0 ? (
          <div className="no-alerts">
            <div className="no-alerts-icon">🛡️</div>

            <div>
              <h3>No Fraud Alerts</h3>

              <p>No high-risk alerts have been received yet.</p>
            </div>
          </div>
        ) : (
          <div className="alerts-list">
            {alerts.slice(0, 10).map((alert, index) => (
              <div className="alert-item" key={index}>
                <div className="alert-icon">🚨</div>

                <div className="alert-content">
                  <div className="alert-top">
                    <strong>{alert.customerId}</strong>

                    <span className="alert-risk">{alert.riskLevel}</span>
                  </div>

                  <div className="alert-details">
                    <span>
                      Amount: ₹{Number(alert.amount).toLocaleString("en-IN")}
                    </span>

                    <span>Risk Score: {alert.riskScore}/100</span>

                    {alert.mlRiskScore !== undefined && (
                      <span>ML Score: {alert.mlRiskScore}</span>
                    )}
                  </div>

                  {alert.reasons?.length > 0 && (
                    <div className="alert-reasons">
                      {alert.reasons.map((reason, reasonIndex) => (
                        <span key={reasonIndex}>{reason}</span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* =========================
          CREATE TRANSACTION
      ========================= */}

      <section className="panel">
        <div className="panel-header">
          <div>
            <h2>Create New Transaction</h2>

            <p>Submit a transaction for real-time fraud analysis</p>
          </div>
        </div>

        <form className="transaction-form" onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Customer ID</label>

            <input
              type="text"
              name="customerId"
              placeholder="Example: CUST1001"
              value={formData.customerId}
              onChange={handleChange}
              required
            />
          </div>

          <div className="input-group">
            <label>Amount</label>

            <input
              type="number"
              name="amount"
              placeholder="Example: 50000"
              value={formData.amount}
              onChange={handleChange}
              required
            />
          </div>

          <div className="input-group">
            <label>Currency</label>

            <input
              type="text"
              name="currency"
              value={formData.currency}
              onChange={handleChange}
              required
            />
          </div>

          <div className="input-group">
            <label>Merchant</label>

            <input
              type="text"
              name="merchant"
              placeholder="Example: Amazon"
              value={formData.merchant}
              onChange={handleChange}
              required
            />
          </div>

          <div className="input-group">
            <label>Location</label>

            <input
              type="text"
              name="location"
              placeholder="Example: Chennai"
              value={formData.location}
              onChange={handleChange}
              required
            />
          </div>

          <div className="input-group">
            <label>Transaction Type</label>

            <select
              name="transactionType"
              value={formData.transactionType}
              onChange={handleChange}
            >
              <option value="ONLINE">ONLINE</option>
              <option value="POS">POS</option>
              <option value="ATM">ATM</option>
            </select>
          </div>

          <div className="input-group">
            <label>Device ID</label>

            <input
              type="text"
              name="deviceId"
              placeholder="Example: DEV001"
              value={formData.deviceId}
              onChange={handleChange}
              required
            />
          </div>

          <button className="create-button" type="submit">
            Analyze Transaction
          </button>
        </form>

        {message && <p className="form-message">{message}</p>}
      </section>

      {/* =========================
          TRANSACTIONS
      ========================= */}

      <section className="panel">
        <div className="panel-header transaction-header">
          <div>
            <h2>Recent Transactions</h2>

            <p>Live transaction risk analysis</p>
          </div>

          <div className="live-indicator">
            <span></span>
            Live Monitoring
          </div>
        </div>

        {transactions.length === 0 ? (
          <div className="empty">
            <div className="empty-icon">📭</div>

            <h3>No transactions available</h3>

            <p>Transactions will appear here in real time.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Amount</th>
                  <th>Merchant</th>
                  <th>Location</th>
                  <th>Type</th>
                  <th>Device</th>
                  <th>Time</th>
                  <th>Risk</th>
                  <th>Level</th>
                  <th>Fraud Indicators</th>
                </tr>
              </thead>

              <tbody>
                {transactions.map((transaction) => {
                  const result = riskResults[transaction.id];

                  return (
                    <tr key={transaction.id}>
                      <td>
                        <strong>{transaction.customerId}</strong>
                      </td>

                      <td className="amount">
                        ₹{Number(transaction.amount).toLocaleString("en-IN")}
                      </td>

                      <td>{transaction.merchant}</td>

                      <td>{transaction.location}</td>

                      <td>
                        <span className="type-badge">
                          {transaction.transactionType}
                        </span>
                      </td>

                      <td>{transaction.deviceId}</td>

                      <td className="timestamp">
                        {transaction.timestamp
                          ? transaction.timestamp.replace("T", " ")
                          : "-"}
                      </td>

                      <td>
                        {result ? (
                          <div className="risk-score">
                            <strong>{result.riskScore}</strong>

                            <span>/100</span>
                          </div>
                        ) : (
                          <span className="analyzing-text">Analyzing...</span>
                        )}
                      </td>

                      <td>
                        <span
                          className={`risk-badge ${getRiskClass(
                            result?.riskLevel,
                          )}`}
                        >
                          {result?.riskLevel || "Analyzing"}
                        </span>
                      </td>

                      <td>
                        {result?.reasons?.length > 0 ? (
                          <div className="fraud-reasons">
                            {result.reasons.map((reason, reasonIndex) => (
                              <span className="reason" key={reasonIndex}>
                                {reason}
                              </span>
                            ))}
                          </div>
                        ) : result ? (
                          <span className="no-risk-reason">
                            No fraud indicators
                          </span>
                        ) : (
                          <span className="analyzing-text">Analyzing...</span>
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

      {/* =========================
          FOOTER
      ========================= */}

      <footer className="footer">
        <p>
          Fraud Detection System • Real-Time Monitoring • Kafka + Spring Boot +
          Python ML + React
        </p>
      </footer>
    </div>
  );
}

export default App;
