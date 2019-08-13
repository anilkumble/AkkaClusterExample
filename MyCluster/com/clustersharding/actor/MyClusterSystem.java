public class MyClusterSystem {

    private static ActorSystem system;

    private static ActorRef supervisorActor;

    private static ActorRef shardRegion;

    public static void main(String args[]) throws Exception{

        system = ActorSystem.create("MyCluster", getConfig());

        supervisorActor =   system.actorOf(Props.create(SupervisorActor.class), "supervisorActor");

        shardRegion = setupClusterSharding(system, "shardRegion", MyProperty.getRole());
    }

    private static Config getConfig() throws Exception{
        Config config;
        if (MyProperty.getPersistence().equals("true")) {
            config = ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.hostname=%s%n", "127.0.0.1") +
                            String.format("akka.remote.netty.tcp.port=%s%n", "2551")) +
                            String.format("redis.host=%s%n", "127.0.0.1") +
                            String.format("redis.port=%s%n", "6379") +
                            String.format("akka.cluster.roles=[%s%n]", "default"))
                    .withFallback(ConfigFactory.load());
        } else {
            config = ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.hostname=%s%n", "127.0.0.1") +
                            String.format("akka.remote.netty.tcp.port=%s%n", "2551") +
                            String.format("akka.persistence.journal.plugin = %s%n","akka.persistence.journal.leveldb") +
                            String.format("akka.persistence.snapshot-store.plugin = %s%n","akka.persistence.snapshot-store.local") +
                            String.format("akka.persistence.journal.leveldb.dir= %s%n", "worker-storage/persistence/journal") +
                            String.format("akka.persistence.snapshot-store.local.dir= %s%n", "worker-storage/persistence/snapshots") +
                            String.format("akka.cluster.roles=[%s%n]","default"))
                    .withFallback(ConfigFactory.load());
        }
        return config;
    }

    public static ActorRef getSupervisorActor(){
        return supervisorActor;
    }

    public static ActorRef getShardRegion(){
        return shardRegion;
    }

    private static ActorRef setupClusterSharding(ActorSystem actorSystem, String entityName, String role) {

        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem).withRole(role);

        return ClusterSharding.get(actorSystem).start(
                entityName,
                ShardingActor.props(),
                settings,
                Message.messageExtractor()
        );

    }

}
