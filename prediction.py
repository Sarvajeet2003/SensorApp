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

# Now, you can use this trained model to predict the roll, pitch, and yaw for the next 20 seconds
# Assuming the timestamp of the last data point in the dataset is the starting point for prediction
start_timestamp = data['Time'].iloc[-1]
end_timestamp = start_timestamp + 20

# Create a DataFrame with timestamps from start to end
future_timestamps = pd.DataFrame({'Time': range(int(start_timestamp), int(end_timestamp) + 1)})

# Make predictions for the future timestamps
future_predictions = model.predict(future_timestamps)

print("Predicted roll, pitch, yaw for the next 20 seconds:")
print(future_predictions)
