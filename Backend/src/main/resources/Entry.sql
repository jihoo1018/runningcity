INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('역사탐방길','풍납토성근린공원 ~ 풍납토성 ~ 몽촌토성 ~ 방이동백제고분군 ~ 석촌동백제초기적석총(지하철8호선 포함)','서울 송파구',11.45,'쉬움','3시간','서울 송파구 장지동',37.475962,127.12539,'공식_산책로',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('인사동삼청동 나들길','인사동 네거리~경복궁매표소~청와대분수앞~북촌한옥마을길','서울 종로구',8.12,'보통','2시간','서울 종로구 세종로 1-7',37.583634,126.974916,'공식_산책로',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('신반포 올레길','총 3km (신잠원나들목 -(1.15km) 신반포5차 오솔길 - (1.50km)반포천나들목)','서울 서초구',3.0,'매우쉬움','60분','서울 서초구 잠원동',37.512126,127.001642,'공식_산책로',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('중랑천제방장미벚꽃길','사각문~묵동수림공원~수림대장미원','서울 중랑구',5.67,'쉬움','2시간','서울 중랑구 중화동',37.594683,127.071106,'공식_산책로',0);

--------------------------------------------------------
-- 🎯 사용자_트랙: 자연어 코스명으로 변경된 부분
--------------------------------------------------------

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('은평 응암나들길','응암1동 일대 순환 산책 코스','은평구 응암1동',15.252,NULL,'약 338분','서울특별시 은평구 응암1동',37.597164,126.91555,'사용자_트랙',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('강남 논현산책길','논현2동 주거지 일대 걷기 코스','강남구 논현2동',5.778,NULL,'약 120분','서울특별시 강남구 논현2동',37.51009,127.035599,'사용자_트랙',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('종로 무악산책길','무악동 주변 도심 산책 코스','종로구 무악동',3.251,NULL,'약 99분','서울특별시 종로구 무악동',37.574158,126.958771,'사용자_트랙',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('송파 거여나들길','거여2동 일대 넓은 도로 중심 산책로','송파구 거여2동',11.34,NULL,'약 178분','서울특별시 송파구 거여2동',37.496956,127.150009,'사용자_트랙',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('강남 논현밤길','논현2동 일대 야간 산책 루트','강남구 논현2동',8.137,NULL,'약 150분','서울특별시 강남구 논현2동',37.506214,127.032677,'사용자_트랙',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('성동 금호산책길','금호1가동 일대 주거지 산책로','성동구 금호1가동',5.791,NULL,'약 113분','서울특별시 성동구 금호1가동',37.553623,127.018654,'사용자_트랙',0);

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address, latitude, longitude, data_source, group_no)
VALUES ('양천 목동순환길','목5동 일대 짧은 순환 산책길','양천구 목5동',3.351,NULL,'약 40분','서울특별시 양천구 목5동',37.535324,126.877777,'사용자_트랙',0);

--------------------------------------------------------
-- group_no = 1 사용자_트랙들
--------------------------------------------------------

INSERT INTO entry (course_nm, course_desc, region, distance_km, difficulty, duration, address,
                   latitude, longitude, data_source, group_no)
VALUES
    ('영등포 봄꽃 나들길', '신갈공원~신길동 넝쿨장미길', '서울 영등포구', 3.84, '매우쉬움', '1시간 30분', '서울 영등포구 신길동', 37.5042413, 126.8994712, '공식_산책로', 1),

    ('홍대 예술의 거리 나들길', '공민왕 사당~와우공원~근현대디자인박물관~홍대걷고싶은길', '서울 마포구', 3.12, '보통', '2시간', '서울 마포구 창전동', 37.5523074, 126.928405, '공식_산책로', 1),

    ('종로 사직동 오솔길', '대림미술관~통의동 백송터~사직단~황학정~성곡미술관~서울역사박물관~경희궁',
     '서울 종로구', 4.6, '보통', '1~2시간', '서울 종로구 세종로 77-9', 37.5758462, 126.9756294, '공식_산책로', 1),

    ('현충원 나들길', '현충원 입구~정국교~박정희대통령묘소입구~호국지장사~육탄10용사 현충비',
     '서울 동작구', 7.34, '보통', '4시간 30분', '서울 동작구 사당동', 37.4937197, 126.9670077, '공식_산책로', 1),

    ('홍릉수목원길', '홍릉수목원 입구~조경인의 숲', '서울 동대문구', 1.83, '쉬움', '1시간', '서울특별시 동대문구 청량리동 회기로 57', 37.5937452, 127.043973, '공식_산책로', 1),

--------------------------------------------------------
-- 사용자_트랙 (group_no = 1)
--------------------------------------------------------

    ('송파 석촌호수길', '석촌동 일대 호수 주변 러닝 및 산책 코스', '송파구 석촌동', 5.334, NULL, '약 92분', '서울특별시 송파구 석촌동', 37.505299, 127.097221, '사용자_트랙', 1),

    ('중랑 신내약수길', '신내2동 일대 공원 및 약수터 순환 코스', '중랑구 신내2동', 12.237, NULL, '약 361분', '서울특별시 중랑구 신내2동', 37.60421, 127.09436, '사용자_트랙', 1),

    ('광진 군자동마을길', '군자동 일대 주택가 산책 코스', '광진구 군자동', 1.23, NULL, '약 50분', '서울특별시 광진구 군자동', 37.550049, 127.07354, '사용자_트랙', 1),

    ('양천 신월산책로', '신월7동 일대 주거지 중심 산책 코스', '양천구 신월7동', 10.151, NULL, '약 230분', '서울특별시 양천구 신월7동', 37.518295, 126.835342, '사용자_트랙', 1),

    ('은평 갈현누리길', '갈현2동 일대 언덕길과 골목길 연결 산책 루트', '은평구 갈현2동', 13.204, NULL, '약 181분', '서울특별시 은평구 갈현2동', 37.61412, 126.915306, '사용자_트랙', 1),

    ('강동 천호하늘길', '천호2동 도심과 공원 연결 산책 코스', '강동구 천호2동', 27.487, NULL, '약 415분', '서울특별시 강동구 천호2동', 37.544685, 127.118973, '사용자_트랙', 1),

    ('멀티캠퍼스 역삼길','역삼동 근처 직장인 산책/런닝 코스','서울 강남구',3.0,'보통','30분','서울 강남구 역삼동',37.503325874722,127.04403462366,'사용자_트랙',1);

--------------------------------------------------------
-- 공식 산책로 (group_no=2) 원본 유지
--------------------------------------------------------

INSERT INTO entry (
    course_nm, course_desc, region, distance_km, difficulty, duration, address,
    latitude, longitude, data_source, group_no
)
VALUES
    ('몽촌토성 나들길',
     '몽촌토성~한성백제박물관~몽촌토성길~88호수',
     '서울 강동구',
     6.14,
     '쉬움',
     '2시간',
     '서울 송파구 방이동',
     37.5156355,
     127.1214926,
     '공식_산책로',
     2
    ),

    ('서울 마포구 A+B코스',
     'A구간 : 1.1km / B구간 : 0.6km 도심 연결 산책 코스',
     '서울 마포구',
     1.7,
     '매우쉬움',
     '40분',
     '서울 마포구 염리동 9-245',
     37.5553554,
     126.9470349,
     '공식_산책로',
     2
    ),

    ('서리골 서리풀 나들길',
     '서리골공원~몽마르뜨공원~서리풀공원',
     '서울 서초구',
     3.25,
     '보통',
     '2시간',
     '서울 서초구 반포동',
     37.4928485,
     127.0016464,
     '공식_산책로',
     2
    ),

    ('도심고궁 나들길',
     '경복궁~국립민속박물관~창덕궁~창경궁~종묘',
     '서울 종로구',
     8.6,
     '보통',
     '3시간 30분',
     '서울 종로구 와룡동',
     37.5821444,
     126.9899013,
     '공식_산책로',
     2
    );
