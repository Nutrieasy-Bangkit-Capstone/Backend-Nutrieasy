package nutrieasy.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Resa S.
 * Date: 29-05-2024
 * Created in IntelliJ IDEA.
 */

@Entity(name = "nutrients")
@Data
public class Nutrients {
    @Id
    @Column(name = "attr_id")
    private int attrID;
    @Column(name = "NFP_2018")
    private int nfp2018;
    @Column(name = "usda_tag")
    private String usdaTag;
    @Column(name = "name")
    private String name;
    @Column(name = "unit")
    private String unit;
    @Column(name = "natural_common")
    private int naturalCommon;
    @Column(name = "item_cpg")
    private int itemCpg;
    @Column(name = "item_restaurant")
    private int itemRestaurant;
    @Column(name = "notes")
    private String notes;
    @Column(name = "bulk_csv_field")
    private String bulkCsvField;
}
