/*Esse Script é um default, provavelmente deverá ser alterado para suportar o ator especifico do cliente.*/

/*==============================================================*/
/* Table: TB_ATOR                                               */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_ATOR (
                                  CO_ATOR    INTEGER     IDENTITY,
                                  CO_USUARIO VARCHAR(60)          NOT NULL,
                                  NO_ATOR    VARCHAR(200),
                                  DS_EMAIL   VARCHAR(200),
                                  CONSTRAINT PK_ATOR PRIMARY KEY (CO_ATOR)
);

/*==============================================================*/
/* View: VW_ATOR                                    */
/*==============================================================*/
create view DBSINGULAR.VW_ATOR AS
SELECT A.CO_ATOR, A.CO_USUARIO as CO_USUARIO, A.NO_ATOR as NO_ATOR, ' ' as DS_EMAIL
FROM DBSINGULAR.TB_ATOR A;
