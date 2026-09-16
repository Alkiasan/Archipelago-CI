package com.alkia.archipelago.util;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.cobblemon.mod.common.item.PokemonItem;
import com.alkia.archipelago.tooltip.PokemonSkinTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;


public class PokemonUtil {
    /**
     * Converts raw skin aspects (e.g., "skin=snowfated") into format skins use sometimes for some reason (e.g., "skin-snowfated")
     */
    public static String normalizeAspect(String raw) {
        return raw == null ? "" : raw.replace("=", "-");
    }

    public static List<String> getWhitelistFromToken(CompoundTag tag) {
        List<String> list = new ArrayList<>();
        if (tag.contains("whitelist", Tag.TAG_LIST)) {
            ListTag tagList = tag.getList("whitelist", Tag.TAG_STRING);
            for (int i = 0; i < tagList.size(); i++) {
                list.add(tagList.getString(i));
            }
        }
        return list;
    }

    public static List<String> getWhitelistAspectsFromToken(CompoundTag tag) {
        List<String> list = new ArrayList<>();
        if (tag.contains("whitelist_aspects", Tag.TAG_LIST)) {
            ListTag tagList = tag.getList("whitelist_aspects", Tag.TAG_STRING);
            for (int i = 0; i < tagList.size(); i++) {
                list.add(normalizeAspect(tagList.getString(i)));
            }
        }
        return list;
    }

    public static List<String> getNormalAspectsFromToken(CompoundTag tag) {
        List<String> list = new ArrayList<>();
        if (tag.contains("aspects", Tag.TAG_LIST)) {
            ListTag tagList = tag.getList("aspects", Tag.TAG_STRING);
            for (int i = 0; i < tagList.size(); i++) {
                list.add(normalizeAspect(tagList.getString(i)));
            }
        }
        return list;
    }

    /**
     * Extracts and Normalized aspects from a Skin Token
     */

    public static List<String> getAspectsFromToken(CompoundTag tokenData) {
        List<String> aspects = new ArrayList<>();
        aspects.addAll(getNormalAspectsFromToken(tokenData));
        aspects.addAll(getWhitelistAspectsFromToken(tokenData));
        return aspects;
    }

    /**
     * Creates an ItemStack for a Pokemon representing the provided Skin Token
     */
    public static ItemStack getPokemonStackFromToken(CompoundTag tag) {
        List<String> whitelist = getWhitelistFromToken(tag);
        if (whitelist.isEmpty()) return ItemStack.EMPTY;

        Species species = getSpecies(whitelist.get(0));
        if (species == null) return ItemStack.EMPTY;

        List<String> aspects = getAspectsFromToken(tag);
        ItemStack pokemonStack = PokemonItem.from(species, new HashSet<>(aspects), 1, null);

        CompoundTag wrapper = new CompoundTag();
        wrapper.put("islander:skin_token", tag);
        pokemonStack.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));

        return pokemonStack;
    }

    /**
     * Creates a TooltipComponent for a Skin Token
     */
    public static Optional<TooltipComponent> createPokemonSkinComponent(CompoundTag skinToken) {
        List<String> pokemonIds = getWhitelistFromToken(skinToken);
        List<String> whitelistAspects = getWhitelistAspectsFromToken(skinToken);
        List<String> aspects = getNormalAspectsFromToken(skinToken);

        if (!pokemonIds.isEmpty() && !aspects.isEmpty()) {
            return Optional.of(new PokemonSkinTooltipComponent(pokemonIds, aspects, whitelistAspects));
        }
        return Optional.empty();
    }

    /**
     * Creates a Pokemon using provided Species and Aspects list
     */
    public static Pokemon createPokemon(String speciesString, List<String> aspects) {
        if (speciesString == null || speciesString.isEmpty()) return null;

        try {
            if (com.alkia.archipelago.config.ModConfig.enableSkinDebug) {
                System.out.println("[ARCHIPELAGO] createPokemon for: " + speciesString + " with aspects: " + aspects);
            }
            PokemonProperties properties = PokemonProperties.Companion.parse(speciesString, " ", "=");
            Pokemon pokemon = new Pokemon();
            properties.apply(pokemon);
            pokemon.initializeMoveset(true);
            pokemon.initialize();
            if (aspects != null) {
                pokemon.getAspects().addAll(aspects);
            }
            return pokemon;
        } catch (Exception e) {
            if (com.alkia.archipelago.config.ModConfig.enableSkinDebug) {
                System.err.println("[ARCHIPELAGO] Error in createPokemon: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }


    /**
     * Logic to determine if a form is Mega
     */
    public static boolean isMegaForm(FormData form) {
        String name = form.getName().toLowerCase();
        boolean hasSpecialAspect = form.getAspects().stream()
                .anyMatch(a -> a.toLowerCase().contains("mega"));
        return name.contains("mega") || hasSpecialAspect;
    }

    /**
     * Logic to determine if a form is Gmax
     */
    public static boolean isGmaxForm(FormData form) {
        String name = form.getName().toLowerCase();
        boolean hasSpecialAspect = form.getAspects().stream()
                .anyMatch(a -> a.toLowerCase().contains("gmax"));
        return name.contains("gmax") || hasSpecialAspect;
    }


    public static String getFormAspect(FormData form) {
        if (form.getAspects() != null && !form.getAspects().isEmpty()) {
            return form.getAspects().get(0).replace("cobblemon:cobblemon:", "cobblemon:");
        }
        return form.getName().toLowerCase();
    }

    /**
     * Returns an array of non-mega/gmax/skin forms
     */
    public static String[] getFilteredForms(Species species) {
        if (species == null || species.getForms() == null) return new String[0];
        return species.getForms().stream()
                .filter(form -> !isMegaForm(form) && !isGmaxForm(form) && !isSkinForm(form))
                .map(PokemonUtil::getFormAspect)
                .toArray(String[]::new);
    }

    /**
     * Logic to determine if a form is a skin
     */
    public static boolean isSkinForm(FormData form) {
        return form.getAspects().stream()
                .anyMatch(a -> a.toLowerCase().startsWith("skin-") || a.toLowerCase().startsWith("skin="));
    }

    /**
     * Returns an array of mega forms
     */
    public static List<String> getMegaForms(Species species) {
        if (species == null || species.getForms() == null) return new ArrayList<>();
        return species.getForms().stream()
                .filter(PokemonUtil::isMegaForm)
                .map(PokemonUtil::getFormAspect)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns an array of gmax forms
     */
    public static List<String> getGmaxForms(Species species) {
        if (species == null || species.getForms() == null) return new ArrayList<>();
        return species.getForms().stream()
                .filter(PokemonUtil::isGmaxForm)
                .map(PokemonUtil::getFormAspect)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
    /**
     * Returns species
     */
    public static Species getSpecies(String speciesString) {
        if (speciesString == null || speciesString.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(speciesString);
        Species species;
        if (rl == null) {
            species = PokemonSpecies.getByName(speciesString);
        } else {
            species = PokemonSpecies.getByIdentifier(rl);
        }
        if (species == null) {
            // Try prefixing with cobblemon: if it fails and has no namespace (was originally used for getting evos for species inside of Item Frames)
            if (!speciesString.contains(":")) {
                species = PokemonSpecies.getByIdentifier(new ResourceLocation("cobblemon", speciesString));
            }
        }
        return species;
    }

    /**
     * Retrieves all available pose names for a given species
     */
    public static List<String> getAvailablePoses(Species species, com.cobblemon.mod.common.client.entity.PokemonClientDelegate delegate) {
        if (species == null) return new ArrayList<>();
        if (delegate == null) return new ArrayList<>();
        com.cobblemon.mod.common.client.render.models.blockbench.PosableModel posableModel =
                com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository.INSTANCE.getPoser(species.getResourceIdentifier(), delegate);
        if (posableModel != null) {
            return new ArrayList<>(posableModel.getPoses().keySet());
        }
        return new ArrayList<>();
    }

    /**
     * Determines the appropriate PoseType for a given pose name and model.
     */
    public static com.cobblemon.mod.common.entity.PoseType getPoseType(String pose, com.cobblemon.mod.common.client.render.models.blockbench.PosableModel model) {
        if (model == null || pose == null || pose.isEmpty()) return null;

        com.cobblemon.mod.common.entity.PoseType type = null;
        if (model.getPoses().containsKey(pose)) {
            com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose poseImpl = model.getPoses().get(pose);
            if (!poseImpl.getPoseTypes().isEmpty()) {
                type = poseImpl.getPoseTypes().iterator().next();
            }
        }

        if (type == null) {
            try {
                type = com.cobblemon.mod.common.entity.PoseType.valueOf(pose.toUpperCase());
            } catch (Exception ignored) {}
        }

        return type;
    }
}
