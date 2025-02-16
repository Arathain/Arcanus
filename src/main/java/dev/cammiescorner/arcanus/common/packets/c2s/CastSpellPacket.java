package dev.cammiescorner.arcanus.common.packets.c2s;

import dev.cammiescorner.arcanus.Arcanus;
import dev.cammiescorner.arcanus.api.ArcanusHelper;
import dev.cammiescorner.arcanus.api.spells.Spell;
import dev.cammiescorner.arcanus.api.spells.SpellComplexity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.PacketSender;

public class CastSpellPacket {
	public static final Identifier ID = Arcanus.id("cast_spell");

	public static void send() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

		ClientSidePacketRegistryImpl.INSTANCE.sendToServer(ID, buf);
	}

	public static void handler(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		server.execute(() -> {
			Spell spell = ArcanusHelper.getSelectedSpell(player);
			if(ArcanusHelper.canCastSpell(player, spell)) {
				if(spell.getSpellComplexity() == SpellComplexity.UNIQUE)
					ArcanusHelper.setUniqueSpellActive(player, spell, !ArcanusHelper.isUniqueSpellActive(player, spell));

				ArcanusHelper.castCurrentSpell(player);
			}
		});
	}
}
