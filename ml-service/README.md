# ml-service

Python inference service. It loads the scikit-learn Random Forest models trained in the original Flask project and serves them at `POST /predict`. The Spring Boot backend is its only caller.

The models were not retrained: the service uses the original project's six pickle files, so its predictions match the original app.

## Setup

The model files are too large for git (two of them are about 1 GB each). Copy the `*.pkl` files from `main_project/bike_forecast/models/` in the [original Flask project](https://github.com/shayan-chakraborty1664/dublin-bikes-team20) into `models/`, then install the dependencies:

```bash
mkdir -p models
cp <original-project>/main_project/bike_forecast/models/*.pkl models/
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

`models/model_meta.json` (the list of features) is committed to git. The models were saved with scikit-learn 1.7.2, so `requirements.txt` pins that version.

## Running

```bash
.venv/bin/uvicorn app:app --port 8001      # loading the models takes about 10 seconds
curl localhost:8001/health
curl -X POST localhost:8001/predict -H 'Content-Type: application/json' \
     -d '{"station_id":42,"capacity":30,"times":["2026-10-28T18:00:00"]}'
```

`times` takes 1 to 48 timestamps. The `weather` field is optional: without it, the weather sub-models estimate the weather from the hour and the month.

With Docker, the models are not baked into the image; mount them at runtime:

```bash
docker build -t dublin-bikes-ml .
docker run -p 8001:8001 -v "$PWD/models:/app/models:ro" dublin-bikes-ml
```
