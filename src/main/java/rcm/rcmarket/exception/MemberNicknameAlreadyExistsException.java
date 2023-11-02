package rcm.rcmarket.exception;

public class MemberNicknameAlreadyExistsException extends RuntimeException {
    public MemberNicknameAlreadyExistsException(String message) {
        super(message);
    }
}
