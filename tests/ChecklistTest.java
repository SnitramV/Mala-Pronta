package br.com.malapronta;
import java.util.*;
public class ChecklistTest {
 static void require(boolean b,String msg) {if(!b)throw new AssertionError(msg);}
 static Checklist.Item find(List<Checklist.Item> all,String id){for(Checklist.Item i:all)if(i.id.equals(id))return i;return null;}
 public static void main(String[] args) {
  Checklist.Trip t=new Checklist.Trip();
  require(Checklist.items(t).size()==78,"Original deve ter 78 itens");
  Set<String> ids=new HashSet<>();for(Checklist.Item i:Checklist.items(t))require(ids.add(i.id),"ID duplicado");
  t.personalized=true;t.days=5;t.stay=1;t.transport=2;t.activity=2;
  List<Checklist.Item> hotel=Checklist.items(t);
  require(hotel.size()==77,"Hotel e avião e praia: 78 - 5 + 2 + 2");
  require(find(hotel,"5-0")==null,"Hotel deve ocultar travesseiro");
  require(find(hotel,"1-0").label.contains("5 unidades"),"Camisetas por dia");
  require(find(hotel,"1-3").label.contains("7 unidades"),"Íntimas extras");
  require(find(hotel,"air-ticket")!=null&&find(hotel,"beach-towel")!=null,"Adições de viagem");
  t.stay=3;t.transport=1;t.activity=3;
  List<Checklist.Item> camp=Checklist.items(t);
  require(camp.size()==84,"Camping e carro e competição: 78 + 3 + 1 + 2");
  require(find(camp,"camp-tent")!=null&&find(camp,"sport-uniform")!=null,"Itens de camping e esporte");
  // Personalization retains stable identities for checked baseline items.
  require(find(camp,"1-0").id.equals(find(hotel,"1-0").id),"Identidade estável");
  for(int stay=0;stay<4;stay++)for(int transport=0;transport<3;transport++)for(int activity=0;activity<4;activity++) {
   t.stay=stay;t.transport=transport;t.activity=activity;ids.clear();
   for(Checklist.Item i:Checklist.items(t))require(ids.add(i.id),"ID duplicado em combinação");
  }
  t.personalized=false;require(Checklist.items(t).size()==78,"Restauração original");
  System.out.println("OK: 78 itens, quantidades, hotel, camping, transporte, atividades, IDs e 48 combinações.");
 }
}
