MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(1, '稻瘟病', '梭形或圆形斑，边缘褐色，中央灰白色，严重时病斑连片', '潮湿时背面有灰色霉层', 1);
MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(2, '纹枯病', '叶鞘及叶片上云纹状大斑，边缘深褐色', '病部可见白色菌丝团或褐色菌核', 2);
MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(3, '白叶枯病', '沿叶缘或叶脉呈黄白色长条状，病部与健部界限呈波浪纹', '湿度大时表面有淡黄色菌脓', 3);
MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(4, '叶烫病', '叶尖或叶缘呈水渍状褪绿，后变黄白色干枯', '病健交界明显，潮湿时有灰色霉层', 4);
MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(5, '胡麻叶斑病', '芝麻粒大小褐色至红褐色斑点，边缘黄色晕圈', '严重时连成不规则大斑', 5);
MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(6, '窄褐斑病', '叶片上出现细窄褐色条斑，宽1-2mm，长可达数厘米', '边缘清晰，严重时密集成片', 6);
MERGE INTO disease_dict (id, name, symptom, spot_feature, sort_order) KEY(id) VALUES
(7, '健康', '叶片健康，无明显病斑', '无', 0);
