public class ChildActor extends AbstractPersistentActor{

    protected ActorRef reply;   

    private boolean recovered = false;

    public ChildActor(ActorRef reply) {
        this.reply      =   reply;
    }

    public void preStart() {
        this.preProcess();
    }

    public void postStop() {
        this.postProcess();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Map.class, recoveredState -> {
                    this.recoveredState    =   recoveredState;

                })
                .build();
    }

    @Override
    public String persistenceId() {
        return getSelf().path().name();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("boot", s ->
                {
                    this.init();
                })
                .build();
    }

    private void init() {
        try {
            /* Business Logic will be handled here */

        } catch (Exception e) {
            /**/
        }
    }

}
