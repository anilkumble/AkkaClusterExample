public class ShardingActor extends AbstractLoggingActor {

    public ShardingActor(){
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(TriggerExecution.class, event -> {

                    ActorRef parentActor = getContext().actorOf(Props.create(ParentActor.class, event, getSelf()), "WA_" + event.getUniqueID());
                    parentActor.tell("boot", getSelf());
                })
                .match(Message.class, message -> {

                    ActorRef childTask = getContext().actorOf(
                            Props.create(Class.forName(message.getIdentifier()), message.getAction(), message.getExecution(), getSender()),
                                    message.getActorName());

                    childTask.tell("boot", getSelf());
                })
                .build();
    }

    private ActorSelection getShardActor(Set< Address > addresses) {
        return getContext().actorSelection(addresses.iterator().next() +"/system/sharding/shardRegion" );
    }

    public static Props props() {
        return Props.create(ShardingActor.class);
    }

    @Override
    public void preStart(){
    }

    @Override
    public void postStop(){

    }

}
