from optimum.onnxruntime import ORTModelForSequenceClassification
from transformers import AutoTokenizer, AutoModel

def save_model():
    hf_tokenizer = AutoTokenizer.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")
    hf_tokenizer.save_pretrained("./huggingface_tokenizer/")

    hf_model = AutoModel.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")
    hf_model.save_pretrained("./huggingface_model/")

def serialize_model():
    ort_model = ORTModelForSequenceClassification.from_pretrained("./huggingface_model", export=True)
    ort_model.save_pretrained("./onnx_model/")

save_model()
serialize_model()
