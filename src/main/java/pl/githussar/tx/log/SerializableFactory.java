package pl.githussar.tx.log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.apache.log4j.Logger;

import pl.githussar.tx.ErrorPhaseExecutor;
import pl.githussar.tx.Operation;

/**
* Created on 20.10.2017 16:25:45
* Copyright © 1993-2017 by ComArch S.A.
*
* Ten plik zrodlowy jest wlasnoscia firmy ComArch S.A. Uzytkownikiem
* tego pliku moze byc jedynie osoba upowazniona przez ComArch S.A. zwylaczeniem
* dostepu osob trzecich.
*
* Osoba, ktora znalazla sie w posiadaniu niniejszego pliku nie posiadajac
* legitymacji prawnej do otrzymania takiego materialu, zobowiazana jest do
* niezwlocznego zwrocenia niniejszego pliku na adres firmy:
*
* ComArch S.A., al. Jana Pawla II 39a, 31-864 Krakow
*
* Rozpowszechnianie, kopiowanie, rozprowadzanie lub inne dzialania opodobnym
* charakterze jest prawnie zabronione pod rygorem sankcji przewidzianych
* w szczegolowych regulacjach prawnych.
*/
/**
*
* @author Piotr for ComArch S.A.
* @version 1.0
*
* Description:
*
*/
public class SerializableFactory {
	
	static final Logger logger = Logger.getLogger(SerializableFactory.class);

	public byte[] serialize(Operation operation) throws IOException{
	   	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(operation);
        oos.close();
        return Base64.getEncoder().encode(baos.toByteArray());
	}
   
	public Operation deserialize(byte[] objectInBase64) throws IOException, NoClassDefFoundError, ClassNotFoundException {
		Operation returnObject = null;
		ByteArrayInputStream  baos = new ByteArrayInputStream (Base64.getDecoder().decode(objectInBase64));
		ObjectInputStream ois = new  ObjectInputStream(baos);
		returnObject = (Operation)ois.readObject();
		return returnObject;
	}
}
