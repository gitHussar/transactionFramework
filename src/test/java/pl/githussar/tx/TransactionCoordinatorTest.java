package pl.githussar.tx;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class TransactionCoordinatorTest {

	@Test
	public void shouldReturnSuccessStatusIfAllOfTwoPhaseOperationSendSuccess(){
		//given
		List<Operation> operations = prepareFullSuccessOperationList();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.OK, status);
	}
	
	@Test
	public void shouldReturnErrorStatusIfAnyOfOperationInPreparingPhaseSendError(){
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.ERROR, status);
	}

	@Test
	public void shouldStopInvokingRestPrepareTansactionIfOneOfThemSendError(){
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(1)).prepareTransaction(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(1)).prepareTransaction(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(1)).prepareTransaction(Matchers.anyString());
		Mockito.verify(operations.get(3),Mockito.times(0)).prepareTransaction(Matchers.anyString());
	}

	@Test
	public void shouldInvokeRollbackOnAllPreperatedOperationIfAnyOfOperationInPreparingPhaseSendError(){
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(1)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(1)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(0)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(3),Mockito.times(0)).rollback(Matchers.anyString());
	}

	private List<Operation> prepareFullSuccessOperationList(){
		List<Operation> operations = new ArrayList<>();
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		return operations;
	}
	
	private List<Operation> prepareOperationFromPreparingPhaseWithErrorStatus(){
		List<Operation> operations = new ArrayList<>();
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.ERROR, operations);
		createPreparingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		return operations;
	}
	
	private void createPreparingPhaseMockOperationAndAddToList(Operation.Status status, List<Operation> operations){
		Operation operation = Mockito.mock(Operation.class);
		when(operation.prepareTransaction(Matchers.anyString())).thenReturn(status);
		operations.add(operation);
	}

}
