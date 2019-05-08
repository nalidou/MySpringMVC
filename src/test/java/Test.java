import com.wzy.mvc.ser.UserSer;

public class Test {

    public static void main(String[] args) {

        try {
            //Class clazz = Class.forName("com.wzy.mvc.ser.UserSer");
            Class clazz = Class.forName("com.wzy.core.annotation.Autowired");
            Object instance = clazz.newInstance();
            //System.out.println(((UserSer)instance).getUser());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
