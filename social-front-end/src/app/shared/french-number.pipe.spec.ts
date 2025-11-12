import { FrenchNumberPipe } from './french-number.pipe';

describe('FrenchNumberPipe', () => {
  let pipe: FrenchNumberPipe;

  beforeEach(() => {
    pipe = new FrenchNumberPipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should format number with space as thousands separator', () => {
    expect(pipe.transform(1000)).toBe('1 000,00');
    expect(pipe.transform(1000000)).toBe('1 000 000,00');
    expect(pipe.transform(999)).toBe('999,00');
  });

  it('should format decimal numbers with comma as decimal separator', () => {
    expect(pipe.transform(1234.56)).toBe('1 234,56');
    expect(pipe.transform(1234.5)).toBe('1 234,50');
  });

  it('should handle string input', () => {
    expect(pipe.transform('1234.56')).toBe('1 234,56');
  });

  it('should handle null and undefined', () => {
    expect(pipe.transform(null)).toBe('0');
    expect(pipe.transform(undefined)).toBe('0');
  });

  it('should respect decimal parameter', () => {
    expect(pipe.transform(1234.567, 0)).toBe('1 235');
    expect(pipe.transform(1234.567, 3)).toBe('1 234,567');
  });
});

