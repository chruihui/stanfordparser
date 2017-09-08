import java.util.Calendar;
import java.util.Date;

/**
 * Created by sssd on 2017/9/8.
 */
public class testDemo {

    public static void main(String[] args) {
        Date date = new Date();
        System.out.println(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -10);
        Date startDate = calendar.getTime();
        System.out.println(startDate);
    }
}
