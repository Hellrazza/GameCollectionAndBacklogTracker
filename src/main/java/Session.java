public class Session {
    private static int activeUUID = -1;

    public  static void  setActiveUUID(int uuid) {
        activeUUID = uuid;
    }

    public static int getActiveUUID() {
        return activeUUID;
    }
}
