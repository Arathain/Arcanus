package dev.cammiescorner.arcanus;

import dev.cammiescorner.arcanus.common.packets.CastSpellMessage;
import dev.cammiescorner.arcanus.common.screens.BookshelfScreenHandler;
import dev.cammiescorner.arcanus.common.structure.processor.BookshelfReplacerStructureProcessor;
import dev.cammiescorner.arcanus.common.structure.processor.LecternStructureProcessor;
import dev.cammiescorner.arcanus.core.integration.ArcanusConfig;
import dev.cammiescorner.arcanus.core.registry.*;
import dev.cammiescorner.arcanus.core.util.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Arcanus implements ModInitializer {
	//-----Custom Registries-----//
	public static final Registry<Spell> SPELL = createRegistry("spell", Spell.class);

	//-----Miscellaneous-----//
	public static ArcanusConfig config;
	public static final String MOD_ID = "arcanus";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "general")).appendItems(list -> {
		ArcanusHelper.addWandsToTab(list);
		Registry.ITEM.forEach(item -> item.appendStacks(Arcanus.ITEM_GROUP, (DefaultedList<ItemStack>) list));
		Arcanus.SPELL.forEach(spell -> list.add(SpellBooks.getSpellBook(spell)));
	}).icon(() -> new ItemStack(ModItems.MASTER_WAND)).build();

	//-----Loot Functions-----//
	public static final LootFunctionType ARCANUS_LOOT_FUNCTION = new LootFunctionType(new ArcanusLootFunction.Serializer());

	//-----Structure Processors-----//
	public static final StructureProcessorType<LecternStructureProcessor> LECTERN_PROCESSOR = StructureProcessorType.register("set_lectern_book", LecternStructureProcessor.CODEC);
	public static final StructureProcessorType<BookshelfReplacerStructureProcessor> BOOKSHELF_PROCESSOR = StructureProcessorType.register("replace_bookshelf", BookshelfReplacerStructureProcessor.CODEC);

	//-----Screen Handlers-----//
	public static final ScreenHandlerType<BookshelfScreenHandler> BOOKSHELF_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(Arcanus.MOD_ID, "fillable_bookshelf"), BookshelfScreenHandler::new);

	@Override
	public void onInitialize() {
		AutoConfig.register(ArcanusConfig.class, JanksonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(ArcanusConfig.class).getConfig();

		ServerPlayNetworking.registerGlobalReceiver(CastSpellMessage.ID, CastSpellMessage::handle);
		Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier(Arcanus.MOD_ID, "arcanus_loot_function"), ARCANUS_LOOT_FUNCTION);

		ModItems.register();
		ModBlocks.register();
		ModBlockEntities.register();
		ModSpells.register();
		ModEntities.register();
		ModParticles.register();
		ModSoundEvents.register();

		EventHandler.commonEvents();

		LOGGER.info("imagine people still looking for these :hahayes:");
	}

	@SuppressWarnings("unchecked")
	private static <T> Registry<T> createRegistry(String name, Class<?> clazz) {
		Registry<?> registry = FabricRegistryBuilder.createSimple(clazz, new Identifier(MOD_ID, name)).buildAndRegister();
		return (Registry<T>) registry;
	}

	public static MutableText getSpellInputs(List<Pattern> pattern, int index) {
		return index >= pattern.size() || pattern.get(index) == null ? new LiteralText("?").formatted(Formatting.GRAY, Formatting.UNDERLINE) : new LiteralText(pattern.get(index).getSymbol()).formatted(Formatting.GREEN);
	}
}
