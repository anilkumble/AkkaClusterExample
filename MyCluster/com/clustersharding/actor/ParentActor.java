public class ParentActor extends AbstractPersistentActor {

	private int count	=	1;	

	private ActorRef sender;

	public ParentActor(TriggerExecution execution, ActorRef sender)
	{
		this.execution = execution;
		this.sender	=	sender;

		if(execution.getStart() != null)
		{
			this.action =	execution.getStart();
		}
	}

	@Override
	public Receive createReceiveRecover() {
		return receiveBuilder()
				.build();
	}

	@Override
	public String persistenceId() {
		return getSelf().path().name();
	}

	@Override
	public Receive createReceive() {

		return receiveBuilder()
				.matchEquals("boot", boot->{
					this.initSharding(this.action);
				})

				.matchEquals("error",error->{
					kill();
				})				
				.build();

	}	
	private void initSharding(Action action){

		String identifier;
		
		try
		{
			ActorSelection shardActor;

			if(SupervisorActor.getNodes().get(action.getType()) != null){
				shardActor	=	getShardActor( SupervisorActor.getNodes().get(action.getType()) );
			}
			else{
				shardActor	=	getShardActor( SupervisorActor.getNodes().get("default") );
			}

			ActionMessage stateMessage = new Message(identifier,getSelf().path().name()+"-action"+this.count++);

			shardActor.tell(stateMessage, getSelf());

		}
		catch (Exception e){
			LOGGER.log(Level.SEVERE, "Error in Sharding", e);
		}

	}
	private ActorSelection getShardActor(Set<Address> addresses) throws Exception {

		Iterator<Address> itr	=	addresses.iterator();

		if(itr.hasNext()){
			return getContext().actorSelection(addresses.iterator().next() +"/system/sharding/shardRegion" );
		}
		throw new Exception("[EXCEPTION] while selecting shard region actor");

	}

	private void kill(){
		getContext().stop(this.sender);
	}	

}
