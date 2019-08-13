public class Message implements Serializable {

    private static final long serialVersionUID 		=	1L;
    
    private String actorName;
    private Class identifier;

    public Message(Class identifier, String actorName) {
        
        this.identifier = identifier;
        this.actorName = actorName;
    }

    public static ShardRegion.MessageExtractor messageExtractor() {

        final int numberOfShards = 100;

        return new ShardRegion.MessageExtractor() {
            @Override
            public String shardId(Object message) {
                return extractShardIdFromCommands(message);
            }

            @Override
            public String entityId(Object message) {
                return extractEntityIdFromCommands(message);
            }

            @Override
            public Object entityMessage(Object message) {
                return message;
            }

            private String extractShardIdFromCommands(Object message) {
                if (message instanceof Message) {
                    return ((Message) message).getActorName().hashCode() % getShardCount(message) + "";
                }
                else if(message instanceof Execution) {
                    return "WA_" + ((Execution) message).getUniqueID().hashCode() % getShardCount(message);
                }
                else{
                    return null;
                }
            }

            private int getShardCount(Object message) {
                int nodeSize    =   30;
                try {
                    if (message instanceof Message) {
                        Task task = ((Message) message).getTask();
                        if (SupervisorActor.getNodes().get(task.getType()) != null){
                            nodeSize = SupervisorActor.getNodes().get(task.getType()).size();
                        }
                        else{
                            nodeSize = SupervisorActor.getNodes().get("default").size();
                        }
                    } else{
                        nodeSize = SupervisorActor.getNodes().size();
                    }
                }catch (Exception e){

                }
                return nodeSize*10;
            }

            private String extractEntityIdFromCommands(Object message) {

                if (message instanceof Message) {
                    return ((Message) message).getActorName();
                }
                else if (message instanceof Execution) {
                    return "WA_"+((Execution) message).getUniqueID();
                }
                else {
                    return null;
                }
            }
        };
    }   

    public String getActorName() {
        return actorName;
    }
	
    public Class getIdentifier() {
        return identifier;
    }
}
