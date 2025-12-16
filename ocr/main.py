from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import easyocr
import shutil
import uuid
import os
import cv2

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

reader = easyocr.Reader(['ko', 'en'], gpu=False)

@app.post("/ocr")
async def run_ocr(file: UploadFile = File(...)):
    temp_filename = f"temp_{uuid.uuid4()}.png"

    with open(temp_filename, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    try:
        img = cv2.imread(temp_filename)

        result = reader.readtext(img)

        texts = [text for (_, text, _) in result]

        return {"full_text": " ".join(texts)}

    except Exception as e:
        return {"error": str(e)}

    finally:
        if os.path.exists(temp_filename):
            os.remove(temp_filename)
