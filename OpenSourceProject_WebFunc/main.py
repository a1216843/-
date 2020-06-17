from flask import Flask, request, jsonify
from gensim.models.doc2vec import Doc2Vec, TaggedDocument
from gensim.test.utils import common_texts
import numpy as np
import pandas as pd
import json
import operator
import random
from haversine import haversine
import matplotlib.pyplot as plt
import seaborn as sns
from ast import literal_eval
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.manifold import TSNE

app = Flask(__name__)

model_title_content = Doc2Vec.load('./datas/model.doc2vec')
data = pd.read_csv('./test_2.csv', encoding='cp949')


def make_doc2vec_data(data, column):
    data_doc = []
    for tag, doc in zip(data.index, data[column]):
        doc = doc.split(" ")
        data_doc.append(([tag], doc))
    data = [TaggedDocument(words=text, tags=tag) for tag, text in data_doc]
    return data


def get_recommend_contents(user, data_doc, model):
    scores = []

    for text, tags in data_doc:
        trained_doc_vec = model.docvecs[tags[0]]
        scores.append(cosine_similarity(user.reshape(-1, 100), trained_doc_vec.reshape(-1, 100)))

    scores = np.array(scores).reshape(-1)
    scores = np.argsort(-scores)[:15]

    return data.loc[scores, :]


def make_user_embedding(index_list, data_doc, model):
    user = []
    user_emdedding = []
    for i in index_list:
        user.append(data_doc[i][1][0])
    for i in user:
        user_emdedding.append(model.docvecs[i])
    user_emdedding = np.array(user_emdedding)
    user = np.mean(user_emdedding, axis=0)
    return user


@app.route("/process", methods=['GET', 'POST'])
def process():
    content = []
    content = request.json
    req_data = pd.DataFrame.from_dict(content, orient='columns')
    data_doc_content = make_doc2vec_data(data, 'feature')
    user = make_user_embedding(req_data.index.values.tolist(), data_doc_content, model_title_content)

    result = get_recommend_contents(user, data_doc_content, model_title_content)


    result = result[['shop_id', 'X', 'Y']]
    result=result.to_json(orient = 'records')

    return jsonify(result)





if __name__ == '__main__':
    app.run('0.0.0.0', port=5000)
