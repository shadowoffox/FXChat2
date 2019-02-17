import java.util.HashMap;
import java.util.Map;

public class AuthServiceImpl implements AuthService{

public Map<String,String> users = new HashMap<>();
    public AuthServiceImpl() {
        users.put("Denis","123");
        users.put("Olga", "456");
        users.put("Dima", "789");
        users.put("Yura","012");

    }

    @Override
    public boolean AuthUser(String username, String password) {
       String pass = users.get(username);
       return pass !=null && pass.equals(password);
    }
}
