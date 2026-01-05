document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("invoiceForm");
  if (!form) return;

  const ELECTRICITY_PRICE = Number(form.dataset.electricityPrice || 0);
  const WATER_PRICE = Number(form.dataset.waterPrice || 0);
  const INTERNET_FEE = Number(form.dataset.internetFee || 0);

  const electricityUsage = document.getElementById("electricityUsage");
  const waterUsage = document.getElementById("waterUsage");

  const electricityFee = document.getElementById("electricityFee");
  const waterFee = document.getElementById("waterFee");
  const totalAmount = document.getElementById("totalAmount");

  const internetFeeInput = document.getElementById("internetFee");
  const internetFeeText = document.getElementById("internetFeeText");

  if (!electricityUsage || !waterUsage || !totalAmount) return;

  const formatVnd = (n) => new Intl.NumberFormat("vi-VN").format(n);

  function calculate() {
    const e = parseInt(electricityUsage.value) || 0;
    const w = parseInt(waterUsage.value) || 0;

    const eFee = e * ELECTRICITY_PRICE;
    const wFee = w * WATER_PRICE;
    const total = eFee + wFee + INTERNET_FEE;

    if (electricityFee) electricityFee.value = formatVnd(eFee);
    if (waterFee) waterFee.value = formatVnd(wFee);

    if (internetFeeInput) internetFeeInput.value = formatVnd(INTERNET_FEE);
    if (internetFeeText) internetFeeText.value = formatVnd(INTERNET_FEE) + " VNĐ ";

    totalAmount.textContent = formatVnd(total) + " VNĐ";
  }

  electricityUsage.addEventListener("input", calculate);
  waterUsage.addEventListener("input", calculate);

  calculate();
});
