package com.blu4ck.topluluk_platform.Service;

import com.blu4ck.topluluk_platform.Model.Invite.InviteCode;
import com.blu4ck.topluluk_platform.Model.User;
import com.blu4ck.topluluk_platform.Supabase.SupabaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InviteService {

    @Autowired
    private SupabaseClient supabaseClient;

    @Autowired
    private MailService mailService;

    @Value("${app.invite.max-per-year}")
    private int maxInvitesPerYear;

    @Value("${app.invite.code-length}")
    private int codeLength;

    @Value("${app.invite.expiration-days}")
    private int expirationDays;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public String generateInviteCode(String inviterId, String invitedEmail, String inviteType, String customMessage) throws IOException {
        // Kullanıcı bilgilerini al
        List<User> users = supabaseClient.select("users?id=eq." + inviterId, User.class);
        if (users.isEmpty()) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }

        User inviter = users.get(0);

        // Admin değilse davet hakkını kontrol et
        if (!"admin".equals(inviteType)) {
            int usedInvites = inviter.getUsedInvites() != null ? inviter.getUsedInvites() : 0;
            if (usedInvites >= maxInvitesPerYear) {
                throw new RuntimeException("Yıllık davet hakkınız dolmuş");
            }
        }

        // Benzersiz kod oluştur
        String code;
        do {
            code = generateRandomCode();
        } while (isCodeExists(code));

        // Davet kodunu kaydet
        InviteCode inviteCode = new InviteCode();
        inviteCode.setInviteCode(code);
        inviteCode.setInviterId(inviterId);
        inviteCode.setInviterName(inviter.getFullName());
        inviteCode.setInviterEmail(inviter.getEmail());
        inviteCode.setInvitedEmail(invitedEmail);
        inviteCode.setInviteType(inviteType);
        inviteCode.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));

        // Admin davetleri başkalarını davet edebilir, user davetleri edemez
        inviteCode.setCanInviteOthers("admin".equals(inviteType));

        supabaseClient.insert("invite_codes", inviteCode, InviteCode.class);

        // Kullanıcının kullanılan davet sayısını artır (admin değilse)
        if (!"admin".equals(inviteType)) {
            inviter.setUsedInvites(inviter.getUsedInvites() + 1);
            supabaseClient.update("users", "id=eq." + inviterId, inviter, User.class);
        }

        // Davet mailini gönder
        mailService.sendInvitationEmail(invitedEmail, code, inviter.getFullName(), customMessage);

        return code;
    }

    public InviteCode validateInviteCode(String code) throws IOException {
        List<InviteCode> codes = supabaseClient.select(
                "invite_codes?invite_code=eq." + code + "&is_used=eq.false&is_active=eq.true",
                InviteCode.class
        );

        if (codes.isEmpty()) {
            throw new RuntimeException("Geçersiz davet kodu");
        }

        InviteCode inviteCode = codes.get(0);

        // Süre kontrolü
        if (inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Davet kodunun süresi dolmuş");
        }

        return inviteCode;
    }

    public void markInviteCodeAsUsed(String code, String userId, String userEmail) throws IOException {
        InviteCode inviteCode = validateInviteCode(code);

        inviteCode.setIsUsed(true);
        inviteCode.setUsedById(userId);
        inviteCode.setUsedByEmail(userEmail);
        inviteCode.setUsedAt(LocalDateTime.now());

        supabaseClient.update("invite_codes", "invite_code=eq." + code, inviteCode, InviteCode.class);
    }

    public boolean canUserInviteOthers(String userId) throws IOException {
        // Kullanıcının davet edilme şeklini kontrol et
        List<InviteCode> usedCodes = supabaseClient.select(
                "invite_codes?used_by_id=eq." + userId + "&is_used=eq.true",
                InviteCode.class
        );

        if (usedCodes.isEmpty()) {
            // Admin tarafından manuel eklenen kullanıcı
            return true;
        }

        InviteCode usedCode = usedCodes.get(0);
        return usedCode.getCanInviteOthers();
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    private boolean isCodeExists(String code) throws IOException {
        List<InviteCode> codes = supabaseClient.select(
                "invite_codes?invite_code=eq." + code,
                InviteCode.class
        );
        return !codes.isEmpty();
    }

    public List<InviteCode> getUserInvites(String userId) throws IOException {
        return supabaseClient.select(
                "invite_codes?inviter_id=eq." + userId + "&is_active=eq.true&order=created_at.desc",
                InviteCode.class
        );
    }
}