package io.github.andregraccia.calcjuros.indicator.service;

import java.util.Arrays;
import java.util.NoSuchElementException;

/** Indicadores suportados e a série do SGS/BCB de cada um. */
public enum Indicator {
    SELIC("selic", "Selic (meta)", 432, "% a.a.", 12),
    CDI("cdi", "CDI", 4389, "% a.a.", 12),
    IPCA("ipca", "IPCA acumulado em 12 meses", 13522, "% em 12 meses", 36),
    POUPANCA("poupanca", "Rendimento da poupança", 195, "% a.m.", 12),
    META_IPCA("meta-ipca", "Meta de inflação (CMN)", 13521, "% a.a.", 36);

    private final String id;
    private final String title;
    private final int series;
    private final String unit;
    private final int months; // janela de histórico buscada no BCB (o SGS limita "ultimos/N" a 20)

    Indicator(String id, String title, int series, String unit, int months) {
        this.id = id;
        this.title = title;
        this.series = series;
        this.unit = unit;
        this.months = months;
    }

    public String id() { return id; }
    public String title() { return title; }
    public int series() { return series; }
    public String unit() { return unit; }
    public int months() { return months; }

    public static Indicator byId(String id) {
        return Arrays.stream(values()).filter(i -> i.id.equals(id)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Indicador desconhecido: " + id));
    }
}
