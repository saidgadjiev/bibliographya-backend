package ru.saidgadjiev.bibliographya.domain.builder;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyCategoryBiography;
import ru.saidgadjiev.bibliographya.html.Header;
import ru.saidgadjiev.bibliographya.html.truncate.HtmlTruncate;
import ru.saidgadjiev.bibliographya.model.BiographyProfession;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCategoryBiographyService;
import ru.saidgadjiev.bibliographya.service.impl.ProfessionService;
import ru.saidgadjiev.bibliographya.utils.NumberUtils;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 23/03/2019.
 */
@Service
public class BiographyBuilder {

    private BiographyCategoryBiographyService biographyCategoryBiographyService;

    private ProfessionService professionService;

    @Autowired
    public BiographyBuilder(BiographyCategoryBiographyService biographyCategoryBiographyService, ProfessionService professionService) {
        this.biographyCategoryBiographyService = biographyCategoryBiographyService;
        this.professionService = professionService;
    }

    public SingleBuilder builder(Biography biography) {
        return new SingleBuilder(biography);
    }

    public MultipleBuilder builder(List<Biography> biographies) {
        return new MultipleBuilder(biographies);
    }

    private void truncateBiography(Biography biography, Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
        Document document = Jsoup.parse(biography.getBio());

        Collection<Header> headers = new ArrayList<>();
        Elements headings = document.select("h1, h2, h3, h4, h5, h6");

        for (Element head : headings) {
            Header header = new Header();

            header.setText(head.text());
            try {
                header.setLevel(NumberUtils.extractInt(head.tag().getName()));

                headers.add(header);
            } catch (NumberFormatException ignored) {

            }
        }

        biography.setHeaders(headers);
        biography.setBio(HtmlTruncate.truncate(biography.getBio(), biographyClampSize));
    }

    public class SingleBuilder {

        private Biography biography;

        private SingleBuilder(Biography biography) {
            this.biography = biography;
        }

        public SingleBuilder buildCategoriesAndProfessions() {
            biography.setCategories(biographyCategoryBiographyService.getBiographyCategories(biography.getId()).getCategories());
            biography.setProfessions(professionService.getBiographyProfessions(biography.getId()).getProfessions());

            return this;
        }

        public SingleBuilder truncateBiography(Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
            if (biographyClampSize != null) {
                BiographyBuilder.this.truncateBiography(biography, biographyClampSize);
            }

            return this;
        }

        public Biography build() {
            return biography;
        }
    }

    public class MultipleBuilder {

        private List<Biography> biographies;

        private MultipleBuilder(List<Biography> biographies) {
            this.biographies = biographies;
        }

        public MultipleBuilder buildCategoriesAndProfessions() {
            Collection<Integer> ids = biographies.stream().map(Biography::getId).collect(Collectors.toList());
            Map<Integer, BiographyCategoryBiography> biographiesCategories = biographyCategoryBiographyService.getBiographiesCategories(ids);
            Map<Integer, BiographyProfession> biographyProfessions = professionService.getBiographiesProfessions(ids);

            for (Biography biography : biographies) {
                biography.setCategories(biographiesCategories.get(biography.getId()).getCategories());
                biography.setProfessions(biographyProfessions.get(biography.getId()).getProfessions());
            }

            return this;
        }

        public MultipleBuilder truncateBiography(Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
            if (biographyClampSize != null) {
                for (Biography biography : biographies) {
                    if (StringUtils.isNotBlank(biography.getBio())) {
                        BiographyBuilder.this.truncateBiography(biography, biographyClampSize);
                    }
                }
            }

            return this;
        }

        public List<Biography> build() {
            return biographies;
        }
    }
}
