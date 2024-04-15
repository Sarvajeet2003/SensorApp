import numpy as np
import matplotlib.dates as mdates
import matplotlib.pyplot as plt
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error

# Load the data from CSV
data = pd.read_csv("/Users/sarvajeethuk/Desktop/orientation_data.csv")

# Convert time to seconds for easier processing
data['Time'] = pd.to_timedelta(data['Time']).dt.total_seconds()

# Split data into features (X) and target variables (y)
X = data[['Time']]
y = data[['Roll', 'Pitch', 'Yaw']]

# Split data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Initialize and train the linear regression model
model = LinearRegression()
model.fit(X_train, y_train)

# Make predictions on the test set
y_pred = model.predict(X_test)

# Calculate the mean squared error
mse = mean_squared_error(y_test, y_pred)
print("Mean Squared Error:", mse)

start_timestamp = data['Time'].iloc[-1]
end_timestamp = start_timestamp + 10

# Create a DataFrame with timestamps from start to end
future_timestamps = pd.DataFrame({'Time': range(int(start_timestamp), int(end_timestamp) + 1)})

# Make predictions for the future timestamps
future_predictions = model.predict(future_timestamps)

# Convert timestamps back to timedelta format
future_timestamps['Time'] = pd.to_timedelta(future_timestamps['Time'], unit='s')

# Create a DataFrame for future predictions including time
future_predictions_df = pd.DataFrame(future_predictions, columns=['Roll', 'Pitch', 'Yaw'])
future_predictions_df['Time'] = future_timestamps['Time']

print("Predicted roll, pitch, yaw for the next 10 seconds:")
print(future_predictions_df)

data = pd.read_csv("/Users/sarvajeethuk/Desktop/orientation_data.csv")

# Generate numerical x-values for actual data and future predictions
num_actual_time = np.arange(len(data['Time']))
num_future_time = np.arange(len(data['Time']), len(data['Time']) + len(future_predictions_df['Time']))

# Plot actual vs predicted values for Roll, Pitch, and Yaw
plt.figure(figsize=(15, 10))

# Plot for Roll
plt.subplot(3, 1, 1)
plt.plot(num_actual_time, data['Roll'], label='Actual Roll', color='blue')
plt.plot(num_future_time, future_predictions_df['Roll'], label='Predicted Roll', linestyle='--', color='orange')
plt.xlabel('Time')
plt.ylabel('Roll')
plt.title('Roll: Actual vs Predicted')
plt.legend()

# Plot for Pitch
plt.subplot(3, 1, 2)
plt.plot(num_actual_time, data['Pitch'], label='Actual Pitch', color='green')
plt.plot(num_future_time, future_predictions_df['Pitch'], label='Predicted Pitch', linestyle='--', color='red')
plt.xlabel('Time')
plt.ylabel('Pitch')
plt.title('Pitch: Actual vs Predicted')
plt.legend()

# Plot for Yaw
plt.subplot(3, 1, 3)
plt.plot(num_actual_time, data['Yaw'], label='Actual Yaw', color='purple')
plt.plot(num_future_time, future_predictions_df['Yaw'], label='Predicted Yaw', linestyle='--', color='brown')
plt.xlabel('Time')
plt.ylabel('Yaw')
plt.title('Yaw: Actual vs Predicted')
plt.legend()

plt.tight_layout()
plt.show()