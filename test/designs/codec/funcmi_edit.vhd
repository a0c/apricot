library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
-- use work.my_package.all;

entity Funcmi is
	port (
		clk : in std_logic;
		data_n : in std_logic_vector(15 downto 0);
		decodcomplete : out std_logic;
		dma : in std_logic;
		ext_adr : in std_logic_vector(9 downto 0);
		ext_in : out std_logic_vector(7 downto 0);
		ext_out : in std_logic_vector(7 downto 0);
		ext_rdwr : in std_logic;
		idle : out std_logic;
		m : in std_logic;
		rst : in std_logic;
		s : in std_logic
	);
end Funcmi;

architecture ARC_Funcmi of Funcmi is

	type FSMStates is (
		a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32, a33, a34, a35, a36, a37, a38, a39, a40, a41, a42, a43, a44, a45, a46, a47, a48, a49, a50, a51, a52, a53, a54, a55, a56, a57, a58, a59, a60, a61, a62, a63, a64, a65, a66, a67, a68, a69, a70, a71, a72, a73, a74, a75, a76, a77, a78, a79, a80, a81, a82, a83, a84, a85, a86, a87, a88, a89, a90, a91, a92, a93, a94, a95, a96, a97, a98, a99, a100, a101, a102, a103, a104, a105, a106, a107, a108, a109, a110, a111, a112
	);
	type m1_type is array (0 to 1023) of std_logic_vector(7 downto 0);
	type m2_type is array (0 to 1023) of std_logic_vector(7 downto 0);
	type m3_type is array (0 to 1023) of std_logic_vector(7 downto 0);

	signal bitcnt : std_logic_vector(7 downto 0);
	signal br : std_logic_vector(7 downto 0);
	signal cnt : std_logic_vector(15 downto 0);
	signal countquart : std_logic_vector(1 downto 0);
	signal currentState : FSMStates;
	signal d1 : std_logic_vector(7 downto 0);
	signal d2 : std_logic_vector(7 downto 0);
	signal d3 : std_logic_vector(7 downto 0);
	signal d4 : std_logic_vector(7 downto 0);
	signal longshifterr : std_logic_vector(15 downto 0);
	signal m1 : m1_type;
	signal m2 : m2_type;
	signal m3 : m3_type;
	signal mac1 : std_logic_vector(9 downto 0);
	signal mac2 : std_logic_vector(9 downto 0);
	signal mac3 : std_logic_vector(9 downto 0);
	signal min1 : std_logic_vector(7 downto 0);
	signal min2 : std_logic_vector(7 downto 0);
	signal min3 : std_logic_vector(7 downto 0);
	signal min4 : std_logic_vector(7 downto 0);
	signal ra1 : std_logic_vector(7 downto 0);
	signal ra2 : std_logic_vector(7 downto 0);
	signal ra3 : std_logic_vector(7 downto 0);
	signal ra4 : std_logic_vector(7 downto 0);
	signal rbyte : std_logic_vector(7 downto 0);
	signal rhalf : std_logic_vector(15 downto 0);
	signal rmask : std_logic_vector(7 downto 0);
	signal rn : std_logic_vector(15 downto 0);
	signal rquart : std_logic_vector(15 downto 0);
	signal shcnt : std_logic_vector(7 downto 0);
	signal shifterl : std_logic_vector(7 downto 0);
	signal shifterr : std_logic_vector(7 downto 0);

begin

	process (clk , rst)

		variable m1_adr : std_logic_vector(9 downto 0);
		variable m2_adr : std_logic_vector(9 downto 0);
		variable m3_adr : std_logic_vector(9 downto 0);

	begin
		if (rst = '1') then
			bitcnt <= (others => '0');
			br <= (others => '0');
			cnt <= (others => '0');
			countquart <= (others => '0');
			d1 <= (others => '0');
			d2 <= (others => '0');
			d3 <= (others => '0');
			d4 <= (others => '0');
			decodcomplete <= '0';
			ext_in <= (others => '0');
			idle <= '0';
			longshifterr <= (others => '0');
			shcnt <= (others => '0');
			shifterl <= (others => '0');
			shifterr <= (others => '0');
			mac1 <= (others => '0');
			mac2 <= (others => '0');
			mac3 <= (others => '0');
			min1 <= (others => '0');
			min2 <= (others => '0');
			min3 <= (others => '0');
			min4 <= (others => '0');
			ra1 <= (others => '0');
			ra2 <= (others => '0');
			ra3 <= (others => '0');
			ra4 <= (others => '0');
			rbyte <= (others => '0');
			rhalf <= (others => '0');
			rmask <= (others => '0');
			rn <= (others => '0');
			rquart <= (others => '0');
			-- for i in m1'range loop
			m1 <= "00000000";
			-- end loop;
			-- for i in m2'range loop
			m2 <= "00000000";
			-- end loop;
			-- for i in m3'range loop
			m3 <= "00000000";
			-- end loop;

			currentState <= a1;
			idle <= '1';

		elsif (clk'event and clk = '1') then
			idle <= '0';

			case currentState is
			when a1 =>
				if (s and dma and ext_rdwr and m) = '1' then
					m3_adr := ext_adr;
					m3(m1_adr) <= ext_out;
					currentState <= a1;
					idle <= '1';

				elsif (s and dma and ext_rdwr and not m) = '1' then
					m1_adr := ext_adr;
					m1(m1_adr) <= ext_out;
					currentState <= a1;
					idle <= '1';

				elsif (s and dma and not ext_rdwr and m) = '1' then
					m3_adr := ext_adr;
					ext_in <= m3(m3_adr);
					currentState <= a1;
					idle <= '1';

				elsif (s and dma and not ext_rdwr and not m) = '1' then
					m1_adr := ext_adr;
					ext_in <= m1(m1_adr);
					currentState <= a1;
					idle <= '1';

				elsif (s and not dma) = '1' then
					rn <= data_n;
					currentState <= a9;

				else
					currentState <= a1;
					idle <= '1';

				end if;

			when a2 =>
				
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a14;

			when a3 =>
				
					m3_adr := mac3;
					m3(m3_adr) <= br;
					currentState <= a15;

			when a4 =>
				
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a20;

			when a5 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a45;

			when a6 =>
				
					m2_adr := mac2;
					m2(m2_adr) <= br;
					currentState <= a33;

			when a7 =>
				
					cnt <= (others => '0');
					currentState <= a36;

			when a8 =>
				if (countquart = "00") then
					ra2 <= min1;
					ra3 <= d1;
					currentState <= a39;

				elsif (not (countquart = "00") and countquart = "01") then
					ra2 <= min2;
					ra3 <= d2;
					currentState <= a39;

				elsif (not (countquart = "00") and not (countquart = "01") and countquart = "10") then
					ra2 <= min3;
					ra3 <= d3;
					currentState <= a39;

				else
					ra2 <= min4;
					ra3 <= d4;
					currentState <= a39;

				end if;

			when a9 =>
				
					rquart <= rn;
					currentState <= a41;

			when a10 =>
				if (s) = '1' then
					mac2 <= (others => '0');
					decodcomplete <= '0';
					currentState <= a2;

				else
					currentState <= a10;

				end if;

			when a11 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a46;

			when a12 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a47;

			when a13 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a48;

			when a14 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a49;

			when a15 =>
				
					mac3 <= std_logic_vector(unsigned(mac3) + 1);
					currentState <= a53;

			when a16 =>
				
					cnt <= (others => '0');
					shcnt <= (others => '0');
					currentState <= a21;

			when a17 =>
				
					shcnt <= (others => '0');
					rmask <= x"80";
					currentState <= a16;

			when a18 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a50;

			when a19 =>
				
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a18;

			when a20 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a51;

			when a21 =>
				
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a56;

			when a22 =>
				
					longshifterr(14 downto 0) <= rquart(15 downto 1);
					longshifterr(15) <= '0';
					currentState <= a24;

			when a23 =>
				
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a27;

			when a24 =>
				
					rquart <= longshifterr(15 downto 0);
					currentState <= a57;

			when a25 =>
				
					rquart <= longshifterr(15 downto 0);
					currentState <= a58;

			when a26 =>
				
					longshifterr(14 downto 0) <= rquart(15 downto 1);
					longshifterr(15) <= '0';
					currentState <= a25;

			when a27 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a52;

			when a28 =>
				
					m2_adr := mac2;
					m2(m2_adr) <= br;
					currentState <= a29;

			when a29 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a61;

			when a30 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a62;

			when a31 =>
				
					m2_adr := mac2;
					m2(m2_adr) <= br;
					currentState <= a30;

			when a32 =>
				
					br <= rhalf(15 downto 8);
					currentState <= a31;

			when a33 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a63;

			when a34 =>
				
					mac2 <= (others => '0');
					br <= "11111111";
					cnt <= (others => '0');
					currentState <= a6;

			when a35 =>
				
					countquart <= std_logic_vector(unsigned(countquart) + 1);
					currentState <= a7;

			when a36 =>
				
					m1_adr := mac1;
					br <= m1(m1_adr);
					currentState <= a70;

			when a37 =>
				
					m2_adr := mac2;
					m2(m2_adr) <= br;
					currentState <= a73;

			when a38 =>
				
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					currentState <= a74;

			when a39 =>
				
					br <= ra2;
					currentState <= a37;

			when a40 =>
				
					m1_adr := mac1;
					br <= m1(m1_adr);
					currentState <= a78;

			when a41 =>
				
					longshifterr(14 downto 0) <= rquart(15 downto 1);
					longshifterr(15) <= '0';
					currentState <= a43;

			when a42 =>
				
					rquart <= longshifterr(15 downto 0);
					currentState <= a59;

			when a43 =>
				
					rquart <= longshifterr(15 downto 0);
					currentState <= a60;

			when a44 =>
				
					longshifterr(14 downto 0) <= rquart(15 downto 1);
					longshifterr(15) <= '0';
					currentState <= a42;

			when a45 =>
				
					rquart(15 downto 8) <= br;
					currentState <= a23;

			when a46 =>
				if (br = "11111111") then
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a12;

				else
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a14;

				end if;

			when a47 =>
				if (br = "11111111") then
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a13;

				else
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a14;

				end if;

			when a48 =>
				if (br = "11111111") then
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a5;

				else
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a14;

				end if;

			when a49 =>
				if (br = "11111111") then
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a11;

				else
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a14;

				end if;

			when a50 =>
				
					ra3 <= br;
					currentState <= a17;

			when a51 =>
				
					ra2 <= br;
					currentState <= a19;

			when a52 =>
				
					rquart(7 downto 0) <= br;
					currentState <= a26;

			when a53 =>
				if (cnt = x"0003") then
					decodcomplete <= '1';
					currentState <= a1;
					idle <= '1';

				else
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a3;

				end if;

			when a54 =>
				
					ra1 <= std_logic_vector(unsigned(rmask) and unsigned(ra4));
					currentState <= a89;

			when a55 =>
				
					bitcnt <= (others => '0');
					currentState <= a54;

			when a56 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a80;

			when a57 =>
				
					countquart <= (others => '0');
					currentState <= a4;

			when a58 =>
				
					rhalf <= rquart;
					currentState <= a22;

			when a59 =>
				
					mac1 <= (others => '0');
					countquart <= (others => '0');
					currentState <= a7;

			when a60 =>
				
					rhalf <= rquart;
					currentState <= a44;

			when a61 =>
				
					mac1 <= (others => '0');
					countquart <= (others => '0');
					currentState <= a8;

			when a62 =>
				
					br <= rhalf(7 downto 0);
					currentState <= a28;

			when a63 =>
				if (cnt = x"0003") then
					rhalf <= rn;
					currentState <= a32;

				else
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a6;

				end if;

			when a64 =>
				
					shifterr(6 downto 0) <= ra3(7 downto 1);
					shifterr(7) <= '0';
					currentState <= a93;

			when a65 =>
				
					shcnt <= (others => '0');
					currentState <= a64;

			when a66 =>
				
					mac1 <= std_logic_vector(unsigned(mac1) + 1);
					currentState <= a94;

			when a67 =>
				
					m1_adr := mac1;
					br <= m1(m1_adr);
					currentState <= a66;

			when a68 =>
				if (cnt = std_logic_vector(unsigned(rquart) - x"0001")) then
					ra3 <= std_logic_vector(unsigned(ra1) - unsigned(ra2));
					currentState <= a65;

				else
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a67;

				end if;

			when a69 =>
				if (br < ra2) then
					ra2 <= br;
					currentState <= a68;

				elsif (not (br < ra2) and cnt = std_logic_vector(unsigned(rquart) - x"0001")) then
					ra3 <= std_logic_vector(unsigned(ra1) - unsigned(ra2));
					currentState <= a65;

				else
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a67;

				end if;

			when a70 =>
				
					mac1 <= std_logic_vector(unsigned(mac1) + 1);
					currentState <= a95;

			when a71 =>
				
					m2_adr := mac2;
					m2(m2_adr) <= br;
					currentState <= a72;

			when a72 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a81;

			when a73 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a82;

			when a74 =>
				
					shifterl(7 downto 1) <= rmask(6 downto 0);
					shifterl(0) <= '0';
					currentState <= a75;

			when a75 =>
				
					rmask <= shifterl;
					currentState <= a97;

			when a76 =>
				
					ra1 <= std_logic_vector(unsigned(rmask) and unsigned(ra4));
					currentState <= a105;

			when a77 =>
				
					shcnt <= (others => '0');
					currentState <= a76;

			when a78 =>
				
					mac1 <= std_logic_vector(unsigned(mac1) + 1);
					currentState <= a96;

			when a79 =>
				
					ra4 <= std_logic_vector(unsigned(ra1) - unsigned(ra2));
					currentState <= a77;

			when a80 =>
				
					ra4 <= br;
					currentState <= a55;

			when a81 =>
				
					shcnt <= (others => '0');
					rmask <= x"01";
					currentState <= a38;

			when a82 =>
				
					br <= ra3;
					currentState <= a71;

			when a83 =>
				
					m3_adr := mac3;
					m3(m3_adr) <= br;
					currentState <= a84;

			when a84 =>
				
					mac3 <= std_logic_vector(unsigned(mac3) + 1);
					currentState <= a107;

			when a85 =>
				
					rbyte <= shifterl;
					currentState <= a108;

			when a86 =>
				if (shcnt = ra3) then
					br <= std_logic_vector(unsigned(rbyte) + unsigned(ra2));
					shcnt <= (others => '0');
					currentState <= a83;

				elsif (not (shcnt = ra3) and bitcnt = x"08" and cnt = rquart and countquart = "11") then
					br <= "11111111";
					cnt <= (others => '0');
					currentState <= a3;

				elsif (not (shcnt = ra3) and bitcnt = x"08" and cnt = rquart and not (countquart = "11")) then
					countquart <= std_logic_vector(unsigned(countquart) + 1);
					currentState <= a4;

				elsif (not (shcnt = ra3) and bitcnt = x"08" and not (cnt = rquart)) then
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a56;

				else
					ra1 <= std_logic_vector(unsigned(rmask) and unsigned(ra4));
					currentState <= a89;

				end if;

			when a87 =>
				
					ra4 <= shifterl;
					currentState <= a110;

			when a88 =>
				
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					bitcnt <= std_logic_vector(unsigned(bitcnt) + 1);
					currentState <= a86;

			when a89 =>
				
					shifterl(7 downto 1) <= ra4(6 downto 0);
					shifterl(0) <= '0';
					currentState <= a87;

			when a90 =>
				if (bitcnt = x"08" and cnt = rquart and countquart = "11") then
					br <= "11111111";
					cnt <= (others => '0');
					currentState <= a3;

				elsif (bitcnt = x"08" and cnt = rquart and not (countquart = "11")) then
					countquart <= std_logic_vector(unsigned(countquart) + 1);
					currentState <= a4;

				elsif (bitcnt = x"08" and not (cnt = rquart)) then
					m2_adr := mac2;
					br <= m2(m2_adr);
					currentState <= a56;

				else
					ra1 <= std_logic_vector(unsigned(rmask) and unsigned(ra4));
					currentState <= a89;

				end if;

			when a91 =>
				if (countquart = "11") then
					min4 <= ra2;
					d4 <= br;
					currentState <= a34;

				elsif (not (countquart = "11") and countquart = "00") then
					min1 <= ra2;
					d1 <= br;
					currentState <= a35;

				elsif (not (countquart = "11") and not (countquart = "00") and countquart = "01") then
					min2 <= ra2;
					d2 <= br;
					currentState <= a35;

				elsif (not (countquart = "11") and not (countquart = "00") and not (countquart = "01") and countquart = "10") then
					min3 <= ra2;
					d3 <= br;
					currentState <= a35;

				else
					countquart <= std_logic_vector(unsigned(countquart) + 1);
					currentState <= a7;

				end if;

			when a92 =>
				if (ra3 = x"00") then
					br <= shcnt;
					currentState <= a91;

				else
					shifterr(6 downto 0) <= ra3(7 downto 1);
					shifterr(7) <= '0';
					currentState <= a93;

				end if;

			when a93 =>
				
					ra3 <= shifterr;
					currentState <= a112;

			when a94 =>
				if (br > ra1) then
					ra1 <= br;
					currentState <= a69;

				elsif (not (br > ra1) and br < ra2) then
					ra2 <= br;
					currentState <= a68;

				elsif (not (br > ra1) and not (br < ra2) and cnt = std_logic_vector(unsigned(rquart) - x"0001")) then
					ra3 <= std_logic_vector(unsigned(ra1) - unsigned(ra2));
					currentState <= a65;

				else
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a67;

				end if;

			when a95 =>
				
					ra1 <= br;
					ra2 <= br;
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a67;

			when a96 =>
				
					ra1 <= br;
					currentState <= a79;

			when a97 =>
				if (shcnt = std_logic_vector(unsigned(ra3) - x"01")) then
					cnt <= (others => '0');
					currentState <= a40;

				else
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					currentState <= a74;

				end if;

			when a98 =>
				
					rbyte <= shifterl;
					currentState <= a109;

			when a99 =>
				if (shcnt = ra3 and cnt = std_logic_vector(unsigned(rquart) - x"0001") and countquart = "11" and (s) = '1') then
					mac2 <= (others => '0');
					decodcomplete <= '0';
					currentState <= a2;

				elsif (shcnt = ra3 and cnt = std_logic_vector(unsigned(rquart) - x"0001") and countquart = "11" and (not s) = '1') then
					currentState <= a99;

				elsif (shcnt = ra3 and cnt = std_logic_vector(unsigned(rquart) - x"0001") and not (countquart = "11")) then
					countquart <= std_logic_vector(unsigned(countquart) + 1);
					currentState <= a8;

				elsif (shcnt = ra3 and not (cnt = std_logic_vector(unsigned(rquart) - x"0001"))) then
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a40;

				else
					ra1 <= std_logic_vector(unsigned(rmask) and unsigned(ra4));
					currentState <= a105;

				end if;

			when a100 =>
				
					mac2 <= std_logic_vector(unsigned(mac2) + 1);
					currentState <= a106;

			when a101 =>
				
					m2_adr := mac2;
					m2(m2_adr) <= br;
					currentState <= a100;

			when a102 =>
				if (bitcnt = x"08") then
					br <= rbyte;
					currentState <= a101;

				elsif (not (bitcnt = x"08") and shcnt = ra3 and cnt = std_logic_vector(unsigned(rquart) - x"0001") and countquart = "11" and (s) = '1') then
					mac2 <= (others => '0');
					decodcomplete <= '0';
					currentState <= a2;

				elsif (not (bitcnt = x"08") and shcnt = ra3 and cnt = std_logic_vector(unsigned(rquart) - x"0001") and countquart = "11" and (not s) = '1') then
					currentState <= a102;

				elsif (not (bitcnt = x"08") and shcnt = ra3 and cnt = std_logic_vector(unsigned(rquart) - x"0001") and not (countquart = "11")) then
					countquart <= std_logic_vector(unsigned(countquart) + 1);
					currentState <= a8;

				elsif (not (bitcnt = x"08") and shcnt = ra3 and not (cnt = std_logic_vector(unsigned(rquart) - x"0001"))) then
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a40;

				else
					ra1 <= std_logic_vector(unsigned(rmask) and unsigned(ra4));
					currentState <= a105;

				end if;

			when a103 =>
				
					ra4 <= shifterl;
					currentState <= a111;

			when a104 =>
				
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					bitcnt <= std_logic_vector(unsigned(bitcnt) + 1);
					currentState <= a102;

			when a105 =>
				
					shifterl(7 downto 1) <= ra4(6 downto 0);
					shifterl(0) <= '0';
					currentState <= a103;

			when a106 =>
				
					bitcnt <= (others => '0');
					rbyte <= x"00";
					currentState <= a99;

			when a107 =>
				
					rbyte <= x"00";
					cnt <= std_logic_vector(unsigned(cnt) + 1);
					currentState <= a90;

			when a108 =>
				if (ra1 = x"00") then
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					bitcnt <= std_logic_vector(unsigned(bitcnt) + 1);
					currentState <= a86;

				else
					rbyte(0) <= '1';
					currentState <= a88;

				end if;

			when a109 =>
				if (ra1 = x"00") then
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					bitcnt <= std_logic_vector(unsigned(bitcnt) + 1);
					currentState <= a102;

				else
					rbyte(0) <= '1';
					currentState <= a104;

				end if;

			when a110 =>
				
					shifterl(7 downto 1) <= rbyte(6 downto 0);
					shifterl(0) <= '0';
					currentState <= a85;

			when a111 =>
				
					shifterl(7 downto 1) <= rbyte(6 downto 0);
					shifterl(0) <= '0';
					currentState <= a98;

			when a112 =>
				
					shcnt <= std_logic_vector(unsigned(shcnt) + 1);
					currentState <= a92;

			end case;

		end if;
	end process;

end ARC_Funcmi;
