//200188J
//A.W.S.M.GUNARATHNE.

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.lang.String;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


interface Wishable
{
    List<Recipients> wishable  = new ArrayList<>();  //Birthday recipient list
    boolean checkdate(String date);                 //compare two dates
}

abstract class Recipients
{
    public String name;
    public String email;
    public String type;
}

class officeNpersonal_friends extends Recipients implements Wishable
{
    String birthday;

    officeNpersonal_friends(String recipient)
    {
        String[] details = recipient.split(",");
        String[] name_type = details[0].split(" ");
        type = name_type[0];
        name = name_type[1];
        if (type.equals("Personal:"))
            email = details[2];
        else if(type.equals("Office_friend:"))
            email=details[1];

        this.birthday = details[3];
    }

    @Override
    public boolean checkdate(String date)
    {
        List<String> currentDate = Arrays.asList(date.split("/"));
        List<String> birthday = Arrays.asList(officeNpersonal_friends.this.birthday.split("/"));

        return (currentDate.get(1) + currentDate.get(2)).equals(birthday.get(1) + birthday.get(2));  //checking the equality of two dates
    }
}

class Office_friends extends officeNpersonal_friends
{
    String designation;

    Office_friends(String recipient)
    {
        super(recipient);
        String[] details = recipient.split(",");
        this.designation = details[2];
    }
}

class Personal_Recipients extends  officeNpersonal_friends
{
    String nickName;

    Personal_Recipients(String recipient)
    {
        super(recipient);
        String[] details = recipient.split(",");
        this.nickName = details[1];
    }
}

class Official_Recipients extends Recipients
{
    String designation;

    Official_Recipients(String recipient){
        String[] details = recipient.split(",");
        String[] name_type = details[0].split(" ");
        type = name_type[0];
        name = name_type[1];
        email = details[1];
        this.designation = details[2];

    }
}
class RecipientsFactory       //factory method
{
    List<Object> Email_Recipient_Obj = new ArrayList<>();   // list of email recipient object that should maintained

    public void ReadFile(File file)
    {
        //Main.recipient_count=0;

        try
        {
            FileInputStream inputFile = new FileInputStream(String.valueOf(file));
            Scanner sc = new Scanner(inputFile);

            while (sc.hasNextLine())
            {
                String email_recipient = sc.nextLine();
                Recipient_Category(email_recipient);
            }
            sc.close();
        }
        catch (FileNotFoundException e)
        {
            //System.out.println("File read was unsuccessful"); Kept empty to make user interface clean
        }
    }
    public void Recipient_Category(String str)
    {
        Main.recipient_count++;
        String[] words = str.split(" ");

        switch (words[0])   //categorizing inputs according to the input
        {
            case "Official:":
                Official_Recipients official_recipients = new Official_Recipients(str);
                Email_Recipient_Obj.add(official_recipients);
                break;

            case "Office_friend:":
                Office_friends office_friends = new Office_friends(str);
                Email_Recipient_Obj.add(office_friends);
                Wishable.wishable.add(office_friends);
                break;

            case "Personal:":
                Personal_Recipients personal_recipients = new Personal_Recipients(str);
                Email_Recipient_Obj.add(personal_recipients);
                Wishable.wishable.add(personal_recipients);
                break;
        }
    }
}

class DateFormatter  //yyyy-mm-dd --> yyyy/mm/dd
{
    public static String Date_Formatter()
    {
        LocalDate dateToday = LocalDate.now();
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return dateToday.format(formatters);
    }
}

class InputValidation       //check the validity of inputs
{
    public static boolean Validation(String date)
    {
        try
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            simpleDateFormat.parse(date);
            return true;
        }
        catch (ParseException e)
        {
            return false;
        }
    }
}

class Email implements Serializable     //Creating email object
{
    String Email_Recipient;
    String Email_Subject;
    String Email_SentDate;

    Email(String recipient,String subject,String sentDate)
    {
        this.Email_Recipient=recipient;
        this.Email_Subject=subject;
        this.Email_SentDate=sentDate;
    }
}

class objSerialize
{
    ArrayList<Object> EmailsOnADay = new ArrayList<>();

    public void ObjectSerialize (Email email_obj)
    {
        try
        {
            FileOutputStream fileOS = new FileOutputStream("emailSaving.ser",true);
            ObjectOutputStream objectOS = new ObjectOutputStream(fileOS);

            objectOS.writeObject(email_obj);

            objectOS.close();
            fileOS.close();

            System.out.println("Serialization done");

        }
        catch (IOException i)
        {
            System.out.println("object serialization was unsuccessful");
        }

    }
    public  void ObjectDeserialize(File file) throws FileNotFoundException
    {
        Email obj;
        FileInputStream fileIS = new FileInputStream(file);
        while (true)
        {
            try
            {
                ObjectInputStream objectIS = new ObjectInputStream(fileIS);
                obj = (Email) objectIS.readObject();
                if (obj != null)
                {
                    EmailsOnADay.add(obj);
                }
                else
                {
                    objectIS.close();
                    fileIS.close();
                }
            }
            catch (IOException | ClassNotFoundException i)
            {
                break;
            }
        }
    }
}

class SendEmail implements Serializable
{
    public void sending(String email_address, String email_subject, String email_body, LocalDate date) throws MessagingException
    {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String MyAccount = "sulakshigunarathne@gmail.com";
        String password = "nkaoomjnregwlokv";

        Session session = Session.getInstance(properties, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(MyAccount, password);
            }
        });

        Message email = prepare_email(session, MyAccount, email_address, email_subject, email_body);

        if (email!= null)
        {
            Transport.send(email);
            DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String converted_date = date.format(formatters);

            Email email_obj = new Email(email_address,email_subject,converted_date);

            System.out.println("Email sent successfully");

            objSerialize serialize = new objSerialize();
            serialize.ObjectSerialize(email_obj);
        }
    }

    private Message prepare_email(Session session, String MyAccount, String recipient, String email_subject, String email_body) {
        Message email=null;
        try {
            email = new MimeMessage((session));
            email.setFrom(new InternetAddress(MyAccount));
            email.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            email.setSubject(email_subject);
            email.setText(email_body);
            System.out.println("Email sending...");
        }
        catch (AddressException e)
        {
            System.out.println("Email address is incorrect");
        }
        catch (MessagingException e)
        {
            System.out.println("Email sent was Unsuccessful");
        }
        return email;
    }
    static boolean email_validation (String email_address)
    {
        boolean valid;

        try
        {
            InternetAddress internetAddress=new InternetAddress(email_address);
            internetAddress.validate();
            valid=true;
        }
        catch (Exception e)
        {
            valid = false;
        }
        return valid;
    }
}

class BirthdayWishes
{
    public void SendBirthdayWishes(LocalDate date_Today) throws MessagingException, FileNotFoundException
    {
        String date = DateFormatter.Date_Formatter();
        List<Recipients> BD_List = Wishable.wishable;

        for (int i = 0; i< BD_List.size();i++)
        {
            if (BD_List.get(i).type.equals("Personal:"))
            {
                Personal_Recipients personal_recipients = (Personal_Recipients) BD_List.get(i);
                if(personal_recipients.checkdate(date))
                {
                    SendEmail send_email = new SendEmail();

                    if (sent_wishes(personal_recipients.email, "Tada!! A Birthday wish", date))
                    {
                        send_email.sending(personal_recipients.email, "Tada!! A Birthday wish", "Wish you a happy birthday with lots of hugs and love \n Sulakshi", date_Today);
                    }
                }
            }
            else if(BD_List.get(i).type.equals("Office_friends"))
            {
                Office_friends office_friends = (Office_friends) BD_List.get(i);
                if (office_friends.checkdate(date.toString()))
                {
                    SendEmail send_email = new SendEmail();
                    if (sent_wishes(office_friends.email, "Birthday wish!!", date))
                    {
                        send_email.sending(office_friends.email, "Birthday wish!!", "Wish you a happy birthday  \n Sulakshi", date_Today);
                    }
                }
            }
        }
    }
    //to filter the b day wishes sent that day when the application turn on repeatedly.
    public boolean sent_wishes(String recipient , String subject , String date ) throws FileNotFoundException
    {
        //deserializing to get the saved email objects
        objSerialize deserialize = new objSerialize();
        try
        {
            deserialize.ObjectDeserialize(new File("emailSaving.ser"));
        }
        catch (FileNotFoundException x)
        {
            //kept empty to give a clean user interface
        }
        finally {
            boolean status = true;
            for (int i = 0; i < deserialize.EmailsOnADay.size(); i++) {
                Email email = (Email) deserialize.EmailsOnADay.get(i);

                if (email.Email_Recipient.equals(recipient) && email.Email_Subject.equals(subject) && email.Email_SentDate.equals(date)) {

                    status = false;
                    break;
                }
            }
            return status;
        }
    }
}

class Write_Details
{
    public void writeDetails (String detail, File file)
    {
        try
        {
            RecipientsFactory recipientsFactory = new RecipientsFactory();
            recipientsFactory.Recipient_Category(detail);

            //open the file in append mode(No overwriting) using BufferedWriter class
            FileWriter filewriter = new FileWriter(file,true);
            BufferedWriter out = new BufferedWriter(filewriter);
            //writing the detail of the recipient to output stream
            out.write(detail + "\n");

            out.close();
            filewriter.close();
        }
        catch (IOException e)
        {
            System.out.println("Error occurred when creating the file");
        }
    }
}

class FindBirthday   //To find a birthday on a given date
{
    public FindBirthday(List<Recipients> wishable, String date)
    {
        if (wishable.size() == 0)
        {
            System.out.println("No any recipient added yet");
        }
        else
        {
            boolean status=false;
            for ( int i=0;i<wishable.size();i++)
            {
                officeNpersonal_friends person = (officeNpersonal_friends) wishable.get(i);

                if (person.checkdate(date))
                {
                    System.out.print(person.name + "\n");
                    status = true;
                }
            }
            if (!status){System.out.println("No one have birthdays on "+date);}
        }
    }
}
class EmailsOnADay  //find sent emails when date given
{
    public void EmailOnGivenDate(String date) throws FileNotFoundException
    {
        objSerialize deserialize = new objSerialize();
        deserialize.ObjectDeserialize(new File("emailSaving.ser"));

        boolean status = false;
        for (int i = 0; i < deserialize.EmailsOnADay.size(); i++)
        {
            Email email = (Email) deserialize.EmailsOnADay.get(i);

            if(email.Email_SentDate.equals(date))
            {
                System.out.println("Recipient is "+((Email) deserialize.EmailsOnADay.get(i)).Email_Recipient + " and Email Subject is " + ((Email) deserialize.EmailsOnADay.get(i)).Email_Subject );
                status = true;
            }
        }
        if (!status)
        {
            System.out.println("No emails sent on the day "+date);
        }
    }
}

public class Main
{
    static int recipient_count=0;

    public static void main(String[] args) throws MessagingException, FileNotFoundException
    {
        File clientList = new File("clientList.txt");
        LocalDate dateToday = LocalDate.now();

        RecipientsFactory RS_F = new RecipientsFactory();
        RS_F.ReadFile(clientList);                           //creating recipient objects while reading file

        //Send birthday greetings automatically
        BirthdayWishes birthdayWishes = new BirthdayWishes();
        birthdayWishes.SendBirthdayWishes(dateToday);

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.println("\nEnter option type: \n"
                    + "1 - Adding a new recipient\n"
                    + "2 - Sending an email\n"
                    + "3 - Printing out all the recipients who have birthdays\n"
                    + "4 - Printing out details of all the emails sent\n"
                    + "5 - Printing out the number of recipient objects in the application\n"
                    + "0 - Terminate the application");

            int option = scanner.nextInt();
            if (option == 0)
            {
                break;
            }
            switch (option)
            {
                case 1:

                    Scanner scanner1 = new Scanner(System.in);
                    System.out.println("Enter recipient in the format\n" +"Official: <name>, <email>,<designation>\n" +
                            "Office_friend: <name>,<email>,<designation>,<birthday>\n" +
                            "Personal: <name>,<nick name>,<email>,<birthday>");

                    //writing the input to the file
                    String format = scanner1.nextLine();
                    Write_Details obj = new Write_Details();
                    obj.writeDetails(format, clientList);
                    birthdayWishes.SendBirthdayWishes(dateToday);  //if newly added has birthday today
                    break;

                case 2:

                    Scanner scanner2 = new Scanner(System.in);
                    System.out.println("Enter email,subject,body");       // input format - email, subject, content

                    // code to send an email
                    int i=0;
                    while (true)
                    {
                        if (i>2) { System.exit(0); }       //if user always gives the invalid format
                        String email = scanner2.nextLine();
                        String[] email_sections = email.split(",");
                        SendEmail send_email = new SendEmail();

                        if (SendEmail.email_validation(email_sections[0]))
                        {
                            //use sending method to send the mail
                            send_email.sending(email_sections[0], email_sections[1], email_sections[2], dateToday);
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid email address , Enter again!");
                            i++;
                        }
                    }
                    break;

                case 3:
                    Scanner scanner3 = new Scanner(System.in);
                    System.out.println("Enter date");          // input format - yyyy/MM/dd (ex: 2018/09/17)

                    // code to print recipients who have birthdays on the given date
                    int j =0;
                    while (true)
                    {
                        if (j>2){System.exit(0);}           //if user always gives the invalid format
                        String date = scanner3.nextLine();

                        if (InputValidation.Validation(date))
                        {
                            new FindBirthday(Wishable.wishable, date);   //to print the recipients who have birthday
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid input. Enter again!");
                            j++;
                        }
                    }
                    break;

                case 4:
                    Scanner scanner4 = new Scanner(System.in);
                    System.out.println("Enter date");

                    // code to print the details of all the emails sent on the input date
                    int k =0;
                    while ((true)){
                        if (k>2){System.exit(0);}           //if user always gives the invalid format
                        String CheckMailDate = scanner4.nextLine();       // input format - yyyy/MM/dd (ex: 2018/09/17)
                        if (InputValidation.Validation(CheckMailDate))
                        {
                            EmailsOnADay emails_on_a_day = new EmailsOnADay();
                            try
                            {
                                //use EmailOnGivenDate method in class EmailsOnADay to print the emails
                                emails_on_a_day.EmailOnGivenDate(CheckMailDate);
                            }
                            catch ( FileNotFoundException e)
                            {
                                System.out.println("No any emails sent yet.");
                            }
                            break;
                        }
                        else
                        {
                            System.out.println("Invalid input. Enter again!");
                            k++;
                        }
                    }
                    break;

                case 5:
                    // code to print the number of recipient objects in the application
                    System.out.println(recipient_count);
                    break;
            }
        }
    }
}

