#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import json
import wave
import os
from vosk import Model, KaldiRecognizer

def recognize_audio(audio_file, model_path):
    try:
        # 检查模型是否存在
        if not os.path.exists(model_path):
            return {"error": "Model not found", "model_path": model_path}

        # 加载模型
        model = Model(model_path)

        # 打开音频文件
        wf = wave.open(audio_file, "rb")
        if wf.getnchannels() != 1 or wf.getsampwidth() != 2 or wf.getcomptype() != "NONE":
            return {"error": "Audio file must be WAV format mono 16 bit"}

        # 创建识别器
        rec = KaldiRecognizer(model, wf.getframerate())
        rec.SetWords(True)

        # 识别音频
        results = []
        while True:
            data = wf.readframes(4000)
            if len(data) == 0:
                break
            if rec.AcceptWaveform(data):
                result = json.loads(rec.Result())
                if result.get("text", "").strip():
                    results.append(result)

        # 获取最终结果
        final_result = json.loads(rec.FinalResult())
        if final_result.get("text", "").strip():
            results.append(final_result)

        wf.close()

        # 合并所有结果
        all_text = " ".join([r.get("text", "") for r in results if r.get("text", "")])

        return {
            "success": True,
            "text": all_text.strip(),
            "results": results
        }

    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(json.dumps({"error": "Usage: python script.py <audio_file> <model_path>"}))
        sys.exit(1)

    audio_file = sys.argv[1]
    model_path = sys.argv[2]

    result = recognize_audio(audio_file, model_path)
    print(json.dumps(result, ensure_ascii=False))
