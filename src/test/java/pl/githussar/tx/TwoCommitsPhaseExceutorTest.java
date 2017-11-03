package pl.githussar.tx;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
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
public class TwoCommitsPhaseExceutorTest {

	@Mock
	private TransactionDAO transactionDAO;
	
	@Mock
	private SerializableFactory serializableFactory;
	
	@Before
	public void setUp(){
		
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
	public void shouldReturnSuccessStatusIfAllOfTwoPhaseOperationSendSuccess() throws TransactionException{
		//given
		List<Operation> operations = prepareFullSuccessOperationListForTwoPhase();
		
		//when
		TwoCommitsPhaseExceutor transactionCoordinator = TwoCommitsPhaseExceutor.createInstance(operations,transactionDAO);
		transactionCoordinator.setSerializableFactory(serializableFactory);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.OK, status);
	}
	
	@Test
	public void shouldReturnErrorStatusIfAnyOfOperationInPreparingPhaseSendError()throws TransactionException{
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TwoCommitsPhaseExceutor transactionCoordinator = TwoCommitsPhaseExceutor.createInstance(operations,transactionDAO);
		transactionCoordinator.setSerializableFactory(serializableFactory);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.ERROR, status);
	}
	
	@Test
	public void shouldReturnErrorStatusIfAnyOfOperationInCommitPhaseSendError() throws TransactionException{
		//given
		List<Operation> operations = prepareOperationFromCommitingPhaseWithErrorStatus();
		
		//when
		TwoCommitsPhaseExceutor transactionCoordinator = TwoCommitsPhaseExceutor.createInstance(operations,transactionDAO);
		transactionCoordinator.setSerializableFactory(serializableFactory);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.ERROR, status);
	}

	@Test
	public void shouldStopCallingRestOfPrepareTransactionIfOneOfThemSendError() throws TransactionException{
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TwoCommitsPhaseExceutor transactionCoordinator =TwoCommitsPhaseExceutor.createInstance(operations,transactionDAO);
		transactionCoordinator.setSerializableFactory(serializableFactory);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(1)).prepareTransaction(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(1)).prepareTransaction(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(1)).prepareTransaction(Matchers.anyString());
		Mockito.verify(operations.get(3),Mockito.times(0)).prepareTransaction(Matchers.anyString());
	}

	@Test
	public void shouldCallRollbackOnAllPreperatedOperationIfAnyOfOperationInPreparingPhaseSendError() throws TransactionException{
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TwoCommitsPhaseExceutor transactionCoordinator = TwoCommitsPhaseExceutor.createInstance(operations,transactionDAO);
		transactionCoordinator.setSerializableFactory(serializableFactory);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(1)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(1)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(1)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(3),Mockito.times(0)).rollback(Matchers.anyString());
	}
	
	@Test
	public void shouldNotCallCommitOnAnyPreperatedOperationIfAnyOfOperationInPreparingPhaseSendError() throws TransactionException{
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TwoCommitsPhaseExceutor transactionCoordinator = TwoCommitsPhaseExceutor.createInstance(operations,transactionDAO);
		transactionCoordinator.setSerializableFactory(serializableFactory);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		for(Operation operation : operations){
			Mockito.verify(operation,Mockito.times(0)).commit(Matchers.anyString());
		}
	}
	
	private List<Operation> prepareFullSuccessOperationListForTwoPhase() throws TransactionException{
		List<Operation> operations = prepareFullSuccessOperationListPreparingPhase();
		
		for (Operation operation : operations){
			when(operation.commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		}
		return operations;
	}
	
	private List<Operation> prepareFullSuccessOperationListPreparingPhase() throws TransactionException{
		List<Operation> operations = new ArrayList<>();
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		return operations;
	}
	
	private List<Operation> prepareOperationFromPreparingPhaseWithErrorStatus() throws TransactionException{
		List<Operation> operations = new ArrayList<>();
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.ERROR, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		return operations;
	}
	
	private List<Operation> prepareOperationFromCommitingPhaseWithErrorStatus() throws TransactionException{
		List<Operation> operations = new ArrayList<>();
		createCommitingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createCommitingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createCommitingPhaseMockOperationAndAddToList(Operation.Status.ERROR, operations);
		
		return operations;
	}
	
	private void createPreparingPhaseMockOperationAndAddToList(Operation.Status status, List<Operation> operations) throws TransactionException{
		Operation operation = Mockito.mock(Operation.class);
		when(operation.prepareTransaction(Matchers.anyString())).thenReturn(status);
		when(operation.prepareTransaction(Matchers.anyString())).thenReturn(status);
		operations.add(operation);

		Map<String,Operation> operationMap = new HashMap<>();
		try {		
			Mockito.doAnswer(new Answer<Object>() {
			    @Override
			    public Object answer(InvocationOnMock invocation) throws Throwable {
			    	byte[] idArray = (byte[]) invocation.getArguments()[0];
			        return operationMap.get(new String(idArray));
			    }
			}).when(serializableFactory).deserialize(Matchers.any(byte[].class));
			
			Mockito.doAnswer(new Answer<Object>() {
			    @Override
			    public Object answer(InvocationOnMock invocation) throws Throwable {
			    	Operation operation = (Operation) invocation.getArguments()[0];
			    	String id = UUID.randomUUID().toString();
			    	operationMap.put(id, operation);
			    	return id.getBytes();
			    }
			}).when(serializableFactory).serialize(Matchers.any(Operation.class));
		} catch (Exception e){
			throw new TransactionException(e.getMessage());
		}
		
	}

	private void createCommitingPhaseMockOperationAndAddToList(Operation.Status status,  List<Operation> operations) throws TransactionException{
		Operation operation = Mockito.mock(Operation.class);
		when(operation.prepareTransaction(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operation.commit(Matchers.anyString())).thenReturn(status);
		operations.add(operation);
		
		Map<String,Operation> operationMap = new HashMap<>();
		try {		
			Mockito.doAnswer(new Answer<Object>() {
			    @Override
			    public Object answer(InvocationOnMock invocation) throws Throwable {
			    	byte[] idArray = (byte[]) invocation.getArguments()[0];
			        return operationMap.get(new String(idArray));
			    }
			}).when(serializableFactory).deserialize(Matchers.any(byte[].class));
			
			Mockito.doAnswer(new Answer<Object>() {
			    @Override
			    public Object answer(InvocationOnMock invocation) throws Throwable {
			    	Operation operation = (Operation) invocation.getArguments()[0];
			    	String id = UUID.randomUUID().toString();
			    	operationMap.put(id, operation);
			    	return id.getBytes();
			    }
			}).when(serializableFactory).serialize(Matchers.any(Operation.class));
		} catch (Exception e){
			throw new TransactionException(e.getMessage());
		}
	}
}
