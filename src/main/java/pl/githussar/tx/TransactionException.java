package pl.githussar.tx;

public class TransactionException extends Exception{

	private static final long serialVersionUID = 1l;
	
	public TransactionException(){
		super();
	}
	
	public TransactionException(String message){
		super(message);
	}
}
