package ru.saidgadjiev.bibliographya;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Operation;

/**
 * Created by said on 11/04/2019.
 */
public class Im4Java {

    public static void main(String[] args) throws Exception {
        ConvertCmd convertCmd = new ConvertCmd();

        convertCmd.setSearchPath("C:\\Program Files\\ImageMagick-7.0.8-Q16");

        Operation operation = new Operation();

        operation.addImage("C:\\Users\\GadzhievSA\\Work\\SCM\\bibliographya-backend\\resources\\share.png");
        operation.addRawArgs("-pointsize", "70");
        operation.addRawArgs("-font", "Arial-Bold");
        operation.addRawArgs("-gravity", "Center");
        operation.addRawArgs("-annotate", "0");
        operation.addRawArgs("Джон Рокфеллер");
        operation.addImage("C:\\Users\\GadzhievSA\\Work\\SCM\\bibliographya-backend\\resources\\share0.png");

        convertCmd.run(operation);
    }
}
