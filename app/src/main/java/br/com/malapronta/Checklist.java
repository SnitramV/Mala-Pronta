package br.com.malapronta;
import java.util.*;

public final class Checklist {
    public static final String[] CATEGORIES = {"Documentos e dinheiro", "Roupas", "Higiene pessoal", "Saúde e primeiros socorros", "Eletrônicos", "Alojamento e conforto", "Festas e rolês", "Extras que salvam"};
    public static final String[] ICONS = {"▣", "♧", "✦", "+", "ϟ", "☾", "♫", "☆"};
    public static final String[][] LABELS = {
        {"RG ou CNH (documento físico)", "Carteirinha universitária", "Cartão de débito/crédito", "Dinheiro em espécie (para emergências)", "Comprovantes de ingresso e alojamento", "Cartão do plano de saúde, se tiver"},
        {"Camisetas para cada dia", "Shorts e bermudas", "Calças", "Roupas íntimas (uma por dia + extras)", "Meias (uma por dia + extras)", "Pijamas", "Moletom ou blusa de frio", "Roupa para voltar da viagem", "Fantasia ou roupa temática das festas", "Roupa de banho (sunga/biquíni)", "Chinelo", "Tênis confortável", "Boné ou óculos de sol"},
        {"Escova de dentes", "Creme dental", "Fio dental", "Desodorante", "Shampoo e condicionador", "Sabonete", "Toalha de banho", "Toalha de rosto", "Perfume", "Pente ou escova de cabelo", "Protetor solar", "Hidratante", "Barbeador e produtos de barbear", "Papel higiênico e lenços umedecidos", "Álcool em gel"},
        {"Medicamentos de uso contínuo", "Medicamentos habituais para dor ou febre", "Curativos e band-aids", "Repelente", "Protetor labial", "Soro fisiológico", "Preservativos", "Sais de reidratação oral"},
        {"Celular", "Carregador de celular", "Power bank", "Cabo USB extra", "Fone de ouvido", "Extensão", "Adaptador de tomada, se necessário"},
        {"Travesseiro", "Lençol", "Fronha", "Cobertor ou manta", "Colchonete ou colchão inflável, se necessário", "Máscara de dormir", "Tampões de ouvido", "Cadeado para mochila ou mala", "Saco para roupas sujas", "Sacolas plásticas", "Garrafa de água reutilizável"},
        {"Abadá ou camiseta da atlética", "Pulseiras e ingressos", "Acessórios para fantasias", "Óculos escuros", "Pochete ou shoulder bag", "Capa de chuva", "Tênis que possa sujar", "Kit de maquiagem ou pintura, se usar", "Identificação da atlética"},
        {"Caneta", "Pequena mochila para passeios", "Guarda-chuva compacto", "Lanches para o trajeto", "Garrafa de água", "Dinheiro separado para emergência", "Kit de costura pequeno", "Tampões extras para ouvido", "Saco estanque ou nécessaire impermeável"}
    };
    public static class Item {
        public final String id, label; public final int category;
        public Item(String id, String label, int category) { this.id=id; this.label=label; this.category=category; }
    }
    public static class Trip {
        public int days=3, stay=0, transport=0, activity=0; public boolean personalized=false;
    }
    public static List<Item> items(Trip t) {
        List<Item> all=new ArrayList<>();
        for(int c=0;c<LABELS.length;c++) for(int i=0;i<LABELS[c].length;i++) {
            // Hotels normally provide bedding; marked items are retained in storage when hidden.
            if(t.personalized && t.stay==1 && c==5 && i<=4) continue;
            String label=LABELS[c][i];
            if(t.personalized && c==1) {
                if(i==0) label="Camisetas • "+t.days+" unidades";
                if(i==3) label="Roupas íntimas • "+(t.days+2)+" unidades";
                if(i==4) label="Meias • "+(t.days+2)+" pares";
                if(i==1) label="Shorts e bermudas • "+Math.max(2,(t.days+1)/2)+" unidades";
            }
            all.add(new Item(c+"-"+i,label,c));
        }
        if(t.personalized) {
            if(t.stay==3) { all.add(new Item("camp-tent","Barraca e lona de proteção",5)); all.add(new Item("camp-light","Lanterna e pilhas",5)); all.add(new Item("camp-sleep","Saco de dormir",5)); }
            if(t.transport==0) all.add(new Item("bus-change","Troca de roupa na mochila de mão",7));
            if(t.transport==1) all.add(new Item("car-doc","Documentos do veículo e CNH do motorista",0));
            if(t.transport==2) { all.add(new Item("air-ticket","Passagem e cartão de embarque",0)); all.add(new Item("air-bag","Conferir limites de bagagem da companhia",7)); }
            if(t.activity==2) { all.add(new Item("beach-towel","Canga ou toalha de praia",6)); all.add(new Item("beach-bag","Sacola para roupa de banho molhada",7)); }
            if(t.activity==3) { all.add(new Item("sport-uniform","Uniforme e equipamento da modalidade",1)); all.add(new Item("sport-shoe","Tênis específico para competição",1)); }
        }
        return all;
    }
}
