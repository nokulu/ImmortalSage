package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.ClientInfo;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.RealmInfo;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.ThirdPartyServerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ReportEnvironment(String clientVersion, @Nullable ReportEnvironment.Server server) {
    public static ReportEnvironment local() {
        return create(null);
    }

    public static ReportEnvironment thirdParty(String pIp) {
        return create(new ReportEnvironment.Server.ThirdParty(pIp));
    }

    public static ReportEnvironment realm(RealmsServer pRealmsServer) {
        return create(new ReportEnvironment.Server.Realm(pRealmsServer));
    }

    public static ReportEnvironment create(@Nullable ReportEnvironment.Server pServer) {
        return new ReportEnvironment(getClientVersion(), pServer);
    }

    public ClientInfo clientInfo() {
        return new ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
    }

    @Nullable
    public ThirdPartyServerInfo thirdPartyServerInfo() {
        return this.server instanceof ReportEnvironment.Server.ThirdParty reportenvironment$server$thirdparty
            ? new ThirdPartyServerInfo(reportenvironment$server$thirdparty.ip)
            : null;
    }

    @Nullable
    public RealmInfo realmInfo() {
        return this.server instanceof ReportEnvironment.Server.Realm reportenvironment$server$realm
            ? new RealmInfo(String.valueOf(reportenvironment$server$realm.realmId()), reportenvironment$server$realm.slotId())
            : null;
    }

    private static String getClientVersion() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("1.21.1");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringbuilder.append(" (modded)");
        }

        return stringbuilder.toString();
    }

    @OnlyIn(Dist.CLIENT)
    public interface Server {
        @OnlyIn(Dist.CLIENT)
        public static record Realm(long realmId, int slotId) implements ReportEnvironment.Server {
            public Realm(RealmsServer pRealmsServer) {
                this(pRealmsServer.id, pRealmsServer.activeSlot);
            }
        }

        @OnlyIn(Dist.CLIENT)
        public static record ThirdParty(String ip) implements ReportEnvironment.Server {
        }
    }
}