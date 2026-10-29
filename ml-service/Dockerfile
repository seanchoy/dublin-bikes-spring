# Python inference service. The model files (about 2 GB) are not baked into the image; they are mounted into /app/models at runtime.
FROM python:3.13-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app.py forecast_model.py ./
ENV MODEL_DIR=/app/models
EXPOSE 8001
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8001"]
