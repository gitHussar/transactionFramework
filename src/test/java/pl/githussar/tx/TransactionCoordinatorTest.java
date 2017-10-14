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
		List<Operation> operations = prepareFullSuccessOperationListForTwoPhase();
		
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
	public void shouldReturnErrorStatusIfAnyOfOperationInCommitPhaseSendError(){
		//given
		List<Operation> operations = prepareOperationFromCommitingPhaseWithErrorStatus();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.ERROR, status);
	}

	@Test
	public void shouldStopCallingRestOfPrepareTransactionIfOneOfThemSendError(){
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
	public void shouldCallRollbackOnAllPreperatedOperationIfAnyOfOperationInPreparingPhaseSendError(){
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
	
	@Test
	public void shouldNotCallCommitOnAnyPreperatedOperationIfAnyOfOperationInPreparingPhaseSendError(){
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(0)).commit(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(0)).commit(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(0)).commit(Matchers.anyString());
		Mockito.verify(operations.get(3),Mockito.times(0)).commit(Matchers.anyString());
	}

	@Test
	public void shouldCallInvertOnAllErrorInRollbackPhase(){
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		when(operations.get(0).rollback(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operations.get(1).rollback(Matchers.anyString())).thenReturn(Operation.Status.ERROR);
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(0)).invert(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(1)).invert(Matchers.anyString());
	}
	
	@Test
	public void shouldCallInvertOnAlreadyCommitedOperationWhenErrorInCommitPhase(){
		//given
		List<Operation> operations = prepareFullSuccessOperationListPreparingPhase();
		when(operations.get(0).commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operations.get(1).commit(Matchers.anyString())).thenReturn(Operation.Status.ERROR);
		when(operations.get(2).commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(1)).invert(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(0)).invert(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(0)).invert(Matchers.anyString());
	}
	
	@Test
	public void shouldCallRollbackOnNotCommitedOperationWhenErrorInCommitPhase(){
		//given
		List<Operation> operations = prepareFullSuccessOperationListPreparingPhase();
		when(operations.get(0).commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operations.get(1).commit(Matchers.anyString())).thenReturn(Operation.Status.ERROR);
		when(operations.get(2).commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Mockito.verify(operations.get(0),Mockito.times(0)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(1),Mockito.times(0)).rollback(Matchers.anyString());
		Mockito.verify(operations.get(2),Mockito.times(1)).rollback(Matchers.anyString());
	}
	
	private List<Operation> prepareFullSuccessOperationListForTwoPhase(){
		List<Operation> operations = prepareFullSuccessOperationListPreparingPhase();
		
		for (Operation operation : operations){
			when(operation.commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		}
		return operations;
	}
	
	private List<Operation> prepareFullSuccessOperationListPreparingPhase(){
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
	
	private List<Operation> prepareOperationFromCommitingPhaseWithErrorStatus(){
		List<Operation> operations = new ArrayList<>();
		createCommitingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createCommitingPhaseMockOperationAndAddToList(Operation.Status.OK, operations);
		createCommitingPhaseMockOperationAndAddToList(Operation.Status.ERROR, operations);
		
		return operations;
	}
	
	private void createPreparingPhaseMockOperationAndAddToList(Operation.Status status, List<Operation> operations){
		Operation operation = Mockito.mock(Operation.class);
		when(operation.prepareTransaction(Matchers.anyString())).thenReturn(status);
		operations.add(operation);
	}

	private void createCommitingPhaseMockOperationAndAddToList(Operation.Status status, List<Operation> operations){
		Operation operation = Mockito.mock(Operation.class);
		when(operation.prepareTransaction(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operation.commit(Matchers.anyString())).thenReturn(status);
		operations.add(operation);
	}
}
