package com.example.qonnect.domain.template;

public class EmailTemplate {


    public static String otpTemplate(String name, String otp) {

        return String.format("""
            Hello %s,

            Your one-time password (OTP) is: %s

            Please use this code to complete your verification. This code will expire in 10 minutes.

            If you did not request this, please ignore this message.

            Thanks,
            The Qonnect Team
            """,name, otp);
    }

}
