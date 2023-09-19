package com.ahmet.model;

import java.io.*;

public interface SerializationUtils {

    static byte[] serialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] serializedData = new byte[]{};
        try(ObjectOutput oo = new ObjectOutputStream(baos)) {
            oo.writeObject(object);
            serializedData = baos.toByteArray();
        } catch (IOException ignored) { }

        return serializedData;
    }

    static Object deserialize(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        Object object = null;
        try(ObjectInput oi = new ObjectInputStream(bais)) {
            object = oi.readObject();
        } catch (IOException | ClassNotFoundException ignored) { }

        return object;
    }
}
