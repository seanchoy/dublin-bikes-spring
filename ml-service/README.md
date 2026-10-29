# ml-service

Python 推理服务：加载原项目训练好的 scikit-learn 随机森林，对外提供 `POST /predict`。Spring Boot 后端是它唯一的调用方。

模型没有重新训练，用的是原 Flask 项目的 6 个 pickle 文件，预测结果和原项目一致。

## 准备（换电脑时）

模型文件太大（两个各约 1GB），不进 git。从原项目拷过来：

```bash
mkdir -p models
cp ../../dublin-bikes-team20/main_project/bike_forecast/models/*.pkl models/
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

`models/model_meta.json`（特征列表）会提交到 git。模型是用 scikit-learn 1.7.2 保存的，所以 `requirements.txt` 固定了这个版本。

## 运行

```bash
.venv/bin/uvicorn app:app --port 8001      # 加载模型约 10 秒
curl localhost:8001/health
curl -X POST localhost:8001/predict -H 'Content-Type: application/json' \
     -d '{"station_id":42,"capacity":30,"times":["2026-10-28T18:00:00"]}'
```

`weather` 字段可选：不传时用天气子模型按“小时 + 月份”推断。
