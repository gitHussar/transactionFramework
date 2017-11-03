package pl.githussar.tx;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.modules.junit4.PowerMockRunner;

import pl.githussar.tx.dao.TransactionDAO;
import pl.githussar.tx.entities.GlobalTransaction;
import pl.githussar.tx.entities.LocalTransaction;
import pl.githussar.tx.log.SerializableFactory;

@RunWith(PowerMockRunner.class)
public class ErrorPhaseExecutorTest {

	@Mock
	private Operation mockOperation;
	
	@Mock
	private Operation mockOperationFailed;
	
	@Mock
	private SerializableFactory serializableFactory;
	
	@Mock
	private TransactionDAO transactionDAO;
	
	@Before
	public void setUp(){
		when(mockOperation.revert(anyString())).thenReturn(Operation.Status.OK);
		when(mockOperationFailed.revert(anyString())).thenReturn(Operation.Status.ERROR);
		
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock invocation) throws Throwable {
		    	GlobalTransaction globalTransaction = (GlobalTransaction) invocation.getArguments()[0];
		        return globalTransaction;
		    }
		}).when(transactionDAO).create(Matchers.any(GlobalTransaction.class));
		
		Mockito.doAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock invocation) throws Throwable {
		    	LocalTransaction localTransaction = (LocalTransaction) invocation.getArguments()[0];
		        return localTransaction;
		    }
		}).when(transactionDAO).updateLocalTransaction(Matchers.any(LocalTransaction.class));
	}
	
	@Test
	public void shouldCallRevertOnAllErrorInRollbackPreparedPhase() throws Exception{
		//given
		List<LocalTransaction> transactionPrepared = prepareTransactionsForRollbackPreparedPhaseWithErrors();
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance(serializableFactory,transactionDAO);
		executorErrorPhase.rollbackPreparedPhase(transactionPrepared);
		
		//then
		Mockito.verify(mockOperation,Mockito.times(transactionPrepared.size())).revert(anyString());
	}
	
	@Test
	public void shouldCallRevertOnAlreadyCommitedOperationWhenErrorInCommitPhase() throws Exception{
		//given
		GlobalTransaction globalTransaction = prepareTransactionsForRollbackCommitedPhase(
				LocalTransaction.Status.COMMITED);
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance(serializableFactory,transactionDAO);
		executorErrorPhase.rollbackCommitedPhase(globalTransaction);
		
		//then
		Mockito.verify(mockOperation,Mockito.times(1)).revert(anyString());
		Mockito.verify(mockOperationFailed,Mockito.times(1)).revert(anyString());
		
	}
	
	@Test
	public void shouldCallRollbackOnNotCommitedOperationWhenErrorInCommitPhase() throws Exception{
		//given
		GlobalTransaction globalTransaction = prepareTransactionsForRollbackCommitedPhase(
				LocalTransaction.Status.PREPARED);
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance(serializableFactory,transactionDAO);
		executorErrorPhase.rollbackCommitedPhase(globalTransaction);
				
		//then
		Mockito.verify(mockOperation,Mockito.times(1)).rollback(anyString());
		Mockito.verify(mockOperationFailed,Mockito.times(1)).rollback(anyString());
	}
	
	private List<LocalTransaction> prepareTransactionsForRollbackPreparedPhaseWithErrors() throws Exception{
		List<LocalTransaction> transactionPrepared = new ArrayList<>();
		
		LocalTransaction singleTransaction = Mockito.mock(LocalTransaction.class);
		when(serializableFactory.deserialize(singleTransaction.getOperation())).thenReturn(mockOperation);
		when(mockOperation.rollback(anyString())).thenReturn(Operation.Status.ERROR);
		
		transactionPrepared.add(singleTransaction);
		transactionPrepared.add(singleTransaction);
		
		return transactionPrepared;
	}
	
	private GlobalTransaction prepareTransactionsForRollbackCommitedPhase(LocalTransaction.Status status) throws Exception{
		GlobalTransaction globalTransaction = new GlobalTransaction();
		List<LocalTransaction> transactionCommited = new ArrayList<>();
	
		Map<String,Operation> operationMap = new HashMap<>();
		
		LocalTransaction singleTransaction = Mockito.mock(LocalTransaction.class);
		String id = UUID.randomUUID().toString();
		operationMap.put(id, mockOperationFailed);
		when(singleTransaction.getOperation()).thenReturn(id.getBytes());
		when(serializableFactory.deserialize(singleTransaction.getOperation())).thenReturn(mockOperationFailed);
		when(singleTransaction.getStatus()).thenReturn(status);
		
		LocalTransaction singleTransaction2 = Mockito.mock(LocalTransaction.class);
		String id2 = UUID.randomUUID().toString();
		operationMap.put(id2, mockOperation);
		when(singleTransaction2.getOperation()).thenReturn(id2.getBytes());
		when(serializableFactory.deserialize(singleTransaction2.getOperation())).thenReturn(mockOperation);
		when(singleTransaction2.getStatus()).thenReturn(status);
		
		transactionCommited.add(singleTransaction);
		transactionCommited.add(singleTransaction2);
		
		globalTransaction.setLocalTransactions(transactionCommited);
		
		try {		
			Mockito.doAnswer(new Answer<Object>() {
			    @Override
			    public Object answer(InvocationOnMock invocation) throws Throwable {
			    	byte[] idArray = (byte[]) invocation.getArguments()[0];
			        return operationMap.get(new String(idArray));
			    }
			}).when(serializableFactory).deserialize(Matchers.any(byte[].class));
		} catch (Exception e){
			throw new TransactionException(e.getMessage());
		}

		return globalTransaction;
		
	}
}
