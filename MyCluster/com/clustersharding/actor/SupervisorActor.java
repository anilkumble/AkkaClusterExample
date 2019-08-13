public class SupervisorActor extends AbstractActor {

    private static Map<String, Set<Address>> nodes = new HashMap<>();

    Cluster cluster = Cluster.get(getContext().system());

    public static Map<String, Set<Address>> getNodes() {
        return nodes;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TriggerExecution.class, event -> {
                    ActorRef parentActor = getContext().actorOf(Props.create(ParentActor.class, event, getSelf()), "WA_" + event.getUniqueID());
                    parentActor.tell("boot", getSelf());
                })
                .match(ClusterEvent.CurrentClusterState.class, state -> {
                    for (Member member : state.getMembers()) {
                        addMember(member);
                    }
                })
                .match(ClusterEvent.MemberUp.class, memberUp -> {
                    addMember(memberUp.member());
                })
                .match(ClusterEvent.MemberExited.class, memberExited -> {
                    removeMember(memberExited.member());
                })
                .build();
    }

    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterEvent.MemberEvent.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    private void addMember(Member member) {
        Set<Address> value = new HashSet<>();
        if (member.roles() != null && member.roles().head() != null && nodes.get(member.roles().head()) != null) {
            value = nodes.get(member.roles().head());
            value.add(member.address());
        } else{
            value.add(member.address());
        }

        nodes.put(member.roles().head(), value);
    }

    private void removeMember(Member member) {
        Set<Address> value = new HashSet<>();
        if (member.roles() != null && member.roles().head() != null && nodes.get(member.roles().head()) != null) {
            value = nodes.get(member.roles().head());
            value.remove(member.address());
        }
        nodes.put(member.roles().head(), value);

    }

    private ActorSelection getShardActor(Set<Address> addresses) {

        if(addresses != null) {
            Iterator<Address> itr = addresses.iterator();
            if (itr.hasNext()){
                return getContext().actorSelection(addresses.iterator().next() + "/system/sharding/shardRegion");
            }
        }
        throw new NoSuchElementException();

    }
}
