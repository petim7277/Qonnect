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



    public static String resetPasswordTemplate(String name, String otp) {
        return String.format("""
            Hello %s,

            We received a request to reset your password.

            Your OTP is: %s

            Enter this code in the app to reset your password. This code will expire in 10 minutes.

            If you did not request a password reset, please ignore this email or contact support.

            Thanks,
            The Qonnect Team
            """, name, otp);
    }

    public static String generateInviteBody(String organizationName, String inviteLink) {
        return String.format("""
        Hello,

        You've been invited to join the organization **%s** on our platform.

        To complete your registration, please click the link below and follow the instructions:

        👉 %s

        This invitation link will expire in **7 days**, so make sure to complete your setup as soon as possible.

        If you did not expect this invitation, you can ignore this message.

        Best regards, \s
        The Qonnect Team
       \s""", organizationName, inviteLink);
    }


}
