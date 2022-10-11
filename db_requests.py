import struct
from db_config import *
import mysql.connector
from image_proc import addFace, updateFace
import numpy as np

db_connection : mysql.connector.MySQLConnection

def bytesToEncoding(inp : bytes):
    if len(inp) != 1024:
        return []
    arr = struct.unpack('d' * 128, inp)
    return np.asarray(arr, np.float64)

def connectToDB():
    global db_connection
    db_connection = mysql.connector.connect(
        host="172.25.115.225",
        user=db_username,
        password=db_password,
        database='big_ochko'
    )

    cursor = db_connection.cursor()
    cursor.execute(f"SELECT id, face_encoding FROM users")
    u = cursor.fetchall()
    print(u)
    for user in u:
        addFace(user[0], bytesToEncoding(user[1]))

def registerUser(email, name, password, face_encoding):
    cursor = db_connection.cursor()
    sql = "INSERT INTO users (email, password_sha256, fullname, face_encoding) VALUES (%s, %s, %s, %s)"
    val = (email, password, name, face_encoding)
    cursor.execute(sql, val)
    db_connection.commit()
    addFace(cursor.lastrowid, bytesToEncoding(face_encoding))
    return cursor.lastrowid

def editUser(uid, name, face_encoding):
    cursor = db_connection.cursor()
    sql = "UPDATE users SET fullname = %s, face_encoding = %s WHERE id = " + str(uid)
    cursor.execute(sql, (name, face_encoding))
    db_connection.commit()
    updateFace(uid, bytesToEncoding(face_encoding))

def findUserByEmail(email):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT * FROM users WHERE (email = \'{email}\')")
    return cursor.fetchall()

def getUsersIdByEmailAndPassword(email, password):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT id FROM users WHERE (email = \'{email}\' and password_sha256 = \'{password}\')")
    ids = cursor.fetchall()
    return (ids[0][0] if len(ids) != 0 else -1)

def getUsersIdByEmail(email):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT id FROM users WHERE (email = \'{email}\')")
    ids = cursor.fetchall()
    return (ids[0][0] if len(ids) != 0 else -1)

def getUserEmailById(id):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT email FROM users WHERE (id = \'{id}\')")
    ids = cursor.fetchall()
    return (ids[0][0] if len(ids) != 0 else 'N/A')

def getPostAddrById(id):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT address FROM posts WHERE (id = \'{id}\')")
    ids = cursor.fetchall()
    return (ids[0][0] if len(ids) != 0 else 'N/A')

def createDelivery(uid1, uid2, post1, post2, droneId):
    cursor = db_connection.cursor()
    sql = "INSERT INTO deliveries (from_user, to_user, from_post, to_post, drone_id) VALUES (%s, %s, %s, %s, %s)"
    val = (uid1, uid2, post1, post2, droneId)
    cursor.execute(sql, val)
    db_connection.commit()
    return cursor.lastrowid

def getPosts():
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT * FROM posts")
    t = cursor.fetchall()
    tt = []
    for i in t:
        tt.append({'id':i[0], 'lat':i[1], 'lon':i[2], 'address':i[3]})
    return tt

def getOrderFromSender(senderId, postId):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT id FROM deliveries WHERE (from_user = {senderId} and from_post = {postId} and status = 0)")
    ids = cursor.fetchall()
    return (ids[0][0] if len(ids) != 0 else -1)

def getOrderFromReceiver(recvId, postId):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT id FROM deliveries WHERE (to_user = {recvId} and to_post = {postId} and status = 1)")
    ids = cursor.fetchall()
    return (ids[0][0] if len(ids) != 0 else -1)

def sentDelivery(id, postId):
    cursor = db_connection.cursor()
    cursor.execute(f"UPDATE deliveries SET status = 1 WHERE (id = {id} and from_post = {postId} and status = 0)")
    db_connection.commit()

def recvdDelivery(id, postId):
    cursor = db_connection.cursor()
    cursor.execute(f"UPDATE deliveries SET status = 2 WHERE (id = {id} and to_post = {postId} and status = 1)")
    db_connection.commit()

def getUserDeliveries(userId):
    cursor = db_connection.cursor()
    cursor.execute(f"SELECT * FROM deliveries WHERE (from_user = {userId} or to_user = {userId})")
    t = cursor.fetchall()
    tt = []
    for i in t:
        tt.append({'id':i[0], 'email_from':getUserEmailById(i[1]), 'email_to':getUserEmailById(i[2]), 'from_post':getPostAddrById(i[3]), 'to_post':getPostAddrById(i[4]), 'status':i[6]})
    return tt


def closeDB():
    db_connection.close()