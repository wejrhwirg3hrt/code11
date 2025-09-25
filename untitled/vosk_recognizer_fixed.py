#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import json
import wave
import os
from vosk import Model, KaldiRecognizer

def recognize_audio(audio_file, model_path):
    try:
        # ���ģ���Ƿ����
        if not os.path.exists(model_path):
            return {"error": "Model not found", "model_path": model_path}

        # ����ģ��
        model = Model(model_path)

        # ����Ƶ�ļ�
        wf = wave.open(audio_file, "rb")
        if wf.getnchannels() != 1 or wf.getsampwidth() != 2 or wf.getcomptype() != "NONE":
            return {"error": "Audio file must be WAV format mono 16 bit"}

        # ����ʶ����
        rec = KaldiRecognizer(model, wf.getframerate())
        rec.SetWords(True)

        # ʶ����Ƶ
        results = []
        while True:
            data = wf.readframes(4000)
            if len(data) == 0:
                break
            if rec.AcceptWaveform(data):
                result = json.loads(rec.Result())
                if result.get("text", "").strip():
                    results.append(result)

        # ��ȡ���ս��
        final_result = json.loads(rec.FinalResult())
        if final_result.get("text", "").strip():
            results.append(final_result)

        wf.close()

        # �ϲ����н��
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
